package ai.h2o.jetty8;

import ai.h2o.webserver.iface.H2OServletContainer;
import ai.h2o.webserver.iface.WebServerConfig;

/**
 * Embedded Jetty instance inside H2O.
 * This is intended to be a singleton per H2O node.
 */
public class Jetty8HTTPD extends AbstractJetty8HTTPD implements H2OServletContainer {
  //------------------------------------------------------------------------------------------
  // Object-specific things.
  //------------------------------------------------------------------------------------------
  static volatile boolean _acceptRequests = false;

  /**
   * Create bare Jetty object.
   */
  public Jetty8HTTPD(WebServerConfig webServerConfig) {
    super(webServerConfig);
  }

  public void acceptRequests() {
    _acceptRequests = true;
  }

}
