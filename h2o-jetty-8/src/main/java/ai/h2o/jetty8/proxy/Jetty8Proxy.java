package ai.h2o.jetty8.proxy;

import ai.h2o.webserver.iface.Credentials;
import ai.h2o.webserver.iface.H2OProxy;
import ai.h2o.webserver.iface.WebServerConfig;

public class Jetty8Proxy implements H2OProxy {


  public Jetty8Proxy(WebServerConfig config, Credentials credentials, String proxyTo) {
//    super(config);
    throw new UnsupportedOperationException(); // TODO
  }

  @Override
  public String getScheme() {
    throw new UnsupportedOperationException(); // TODO
  }

  @Override
  public int getPort() {
    throw new UnsupportedOperationException(); // TODO
  }

  @Override
  public void start(String ip, int port) throws Exception {
    throw new UnsupportedOperationException(); // TODO
  }
}
