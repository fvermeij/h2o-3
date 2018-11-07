package ai.h2o.jetty8.proxy;

import ai.h2o.jetty8.AbstractJetty8HTTPD;
import ai.h2o.webserver.iface.Credentials;
import ai.h2o.webserver.iface.H2OProxy;
import ai.h2o.webserver.iface.WebServerConfig;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Jetty8Proxy extends AbstractJetty8HTTPD implements H2OProxy {

  private final String _proxyTo;
  private final Credentials _credentials;

  public Jetty8Proxy(WebServerConfig params, Credentials credentials, String proxyTo) {
    super(params);
    _proxyTo = proxyTo;
    _credentials = credentials;
  }

  @Override
  protected void registerHandlers(HandlerWrapper handlerWrapper, ServletContextHandler context) {
    // setup authenticating proxy servlet (each request is forwarded with BASIC AUTH)
    final ServletHolder proxyServlet = new ServletHolder(TransparentProxyServlet.class);
    proxyServlet.setInitParameter("ProxyTo", _proxyTo);
    proxyServlet.setInitParameter("Prefix", "/");
    proxyServlet.setInitParameter("BasicAuth", _credentials.toBasicAuth());
    context.addServlet(proxyServlet, "/*");
    // authHandlers assume the user is already authenticated
    final HandlerCollection authHandlers = new HandlerCollection();
    authHandlers.setHandlers(new Handler[]{
            new AuthenticationHandler(),
            context,
    });
    // handles requests of login form and delegates the rest to the authHandlers
    final ProxyLoginHandler loginHandler = new ProxyLoginHandler("/login", "/loginError");
    loginHandler.setHandler(authHandlers);
    // login handler is the root handler
    handlerWrapper.setHandler(loginHandler);
  }

  @Override
  protected RuntimeException failEx(String message) {
    return new IllegalStateException(message);
  }

}
