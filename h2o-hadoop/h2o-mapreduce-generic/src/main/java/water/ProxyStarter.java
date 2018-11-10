package water;

import ai.h2o.webserver.iface.Credentials;
import ai.h2o.webserver.iface.H2OProxy;
import ai.h2o.webserver.iface.H2OServletContainerLoader;
import ai.h2o.webserver.iface.WebServerConfig;
import water.init.HostnameGuesser;
import water.init.NetworkInit;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

public class ProxyStarter {

  public static String start(String[] args, Credentials credentials, String proxyTo,
                             boolean useHostname) {
    if (! proxyTo.endsWith("/")) {
      proxyTo = proxyTo + "/";
    }

    final H2O.OptArgs baseArgs = H2O.parseH2OArgumentsTo(args, new H2O.OptArgs());
    final WebServerConfig config = NetworkInit.webServerParams(baseArgs);
    final H2OProxy proxy = initializeProxy(config, credentials, proxyTo);

    InetAddress address = HostnameGuesser.findInetAddressForSelf(baseArgs.ip, baseArgs.network);
    if (useHostname) {
      String hostname = localIpToHostname(address);
      return H2O.getURL(proxy.getScheme(), hostname, proxy.getPort(), baseArgs.context_path);
    } else {
      return H2O.getURL(proxy.getScheme(), address, proxy.getPort(), baseArgs.context_path);
    }
  }

  private static String localIpToHostname(InetAddress address) {
    String hostname = address.getHostName();
    if (! address.getHostAddress().equals(hostname))
      return hostname;
    // we don't want to return IP address (because of a security policy of a particular customer, see PUBDEV-5680)
    hostname = System.getenv("HOSTNAME");
    if ((hostname == null) || hostname.isEmpty())
      hostname = "localhost";
    System.out.println("WARN: Proxy IP address couldn't be translated to a hostname. Using environment variable HOSTNAME='" + hostname + "' as a fallback.");
    return hostname;
  }

  private static H2OProxy initializeProxy(WebServerConfig config, Credentials credentials, String proxyTo) {
    int proxyPort = config.port == 0 ? config.baseport : config.port;

    final H2OProxy proxy = H2OServletContainerLoader.INSTANCE.createProxy(config, credentials, proxyTo);

    // PROXY socket is only used to find opened port on given ip
    ServerSocket proxySocket = null;

    while (true) {
      try {
        proxySocket = config.web_ip == null ? // Listen to any interface
                new ServerSocket(proxyPort) : new ServerSocket(proxyPort, -1, getInetAddress(config.web_ip));
        proxySocket.setReuseAddress(true);

        // race condition: another process can use the port while proxy is starting
        proxySocket.close();
        proxy.start(config.web_ip, proxyPort);

        break;
      } catch (Exception e) {
        for (Throwable ee = e; ee != null; ee = ee.getCause()) {
          if (ee instanceof GeneralSecurityException) {
            throw new RuntimeException("Proxy initialization failed (check keystore password)", e);
          }
        }
        System.err.println("TRACE: Cannot allocate API port " + proxyPort + " because of following exception: " + e.getMessage());
        if (proxySocket != null) try {
          proxySocket.close();
        } catch (IOException ee) {
          System.err.println("TRACE: " + ee.getMessage());
        }
        proxySocket = null;
        if (config.port != 0) {
          throw new RuntimeException("Port " + proxyPort + " is not available, change -port PORT and try again.");
        }
      }
      // Try next available port to bound
      proxyPort += 2;
      if (proxyPort > (1 << 16)) {
        throw new RuntimeException("Cannot find free port from baseport = " + config.baseport);
      }
    }

    return proxy;
  }

  private static InetAddress getInetAddress(String ip) {
    if (ip == null) {
      return null;
    }

    try {
      return InetAddress.getByName(ip);
    } catch (UnknownHostException e) {
      throw new RuntimeException("Unable to resolve the host", e);
    }
  }

  // just for local testing
  public static void main(String[] args) {
    Credentials cred = Credentials.make(System.getProperty("user.name"), "Heslo123");
    String url = start(args, cred, "https://localhost:54321/", false);
    System.out.println("Proxy started on " + url + " " + cred.toDebugString());
  }

}
