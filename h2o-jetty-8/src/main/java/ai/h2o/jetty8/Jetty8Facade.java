package ai.h2o.jetty8;

import ai.h2o.jetty8.proxy.Jetty8Proxy;
import ai.h2o.webserver.iface.Credentials;
import ai.h2o.webserver.iface.H2OHttpServer;
import ai.h2o.webserver.iface.H2OProxy;
import ai.h2o.webserver.iface.H2OServletContainer;
import ai.h2o.webserver.iface.H2OServletContainerFacade;
import ai.h2o.webserver.iface.WebServerConfig;

public class Jetty8Facade implements H2OServletContainerFacade {
  @Override
  public H2OServletContainer createServletContainer(H2OHttpServer h2OHttpServer) {
    return new Jetty8Adapter(h2OHttpServer);
  }

  @Override
  public H2OProxy createProxy(WebServerConfig args, Credentials credentials, String proxyTo) {
    return new Jetty8Proxy(args, credentials, proxyTo);
  }
}
