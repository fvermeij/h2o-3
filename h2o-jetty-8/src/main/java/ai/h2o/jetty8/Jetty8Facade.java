package ai.h2o.jetty8;

import ai.h2o.jetty8.proxy.Jetty8Proxy;
import water.server.Credentials;
import water.server.H2OProxy;
import water.server.H2OServletContainer;
import water.server.H2OServletContainerFacade;
import water.server.WebServerConfig;

public class Jetty8Facade implements H2OServletContainerFacade {
  @Override
  public H2OServletContainer createServletContainer(WebServerConfig params) {
    return new Jetty8HTTPD(params);
  }

  @Override
  public H2OProxy createProxy(WebServerConfig args, Credentials credentials, String proxyTo) {
    return new Jetty8Proxy(args, credentials, proxyTo);
  }
}
