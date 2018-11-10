package ai.h2o.jetty8;

import ai.h2o.webserver.iface.Credentials;
import ai.h2o.webserver.iface.H2OHttpServer;
import ai.h2o.webserver.iface.H2OProxy;
import ai.h2o.webserver.iface.H2OServletContainer;
import ai.h2o.webserver.iface.H2OServletContainerFacade;

public class Jetty8Facade implements H2OServletContainerFacade {
  @Override
  public H2OServletContainer createServletContainer(H2OHttpServer h2OHttpServer) {
    return new Jetty8Adapter(h2OHttpServer);
  }

  @Override
  public H2OProxy createProxy(H2OHttpServer h2oHttpServer, Credentials credentials, String proxyTo) {
    return Jetty8Adapter.createProxyAdapter(h2oHttpServer, credentials, proxyTo);
  }
}
