package ai.h2o.jetty8;

import ai.h2o.webserver.iface.H2OServletContainer;
import ai.h2o.webserver.iface.WebServerConfig;
import water.api.RequestServer;
import water.server.ServletUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Embedded Jetty instance inside H2O.
 * This is intended to be a singleton per H2O node.
 */
public class Jetty8HTTPD extends AbstractJetty8HTTPD implements H2OServletContainer {
  //------------------------------------------------------------------------------------------
  // Object-specific things.
  //------------------------------------------------------------------------------------------
  private static volatile boolean _acceptRequests = false;

  /**
   * Create bare Jetty object.
   */
  public Jetty8HTTPD(WebServerConfig webServerConfig) {
    super(webServerConfig);
  }

  public void acceptRequests() {
    _acceptRequests = true;
  }

  static void gateHandler(HttpServletRequest request, HttpServletResponse response) {
    ServletUtils.startRequestLifecycle();
    while (!_acceptRequests) {
      try { Thread.sleep(100); }
      catch (Exception ignore) {}
    }

    boolean isXhrRequest = false;
    if (request != null) {
      isXhrRequest = ServletUtils.isXhrRequest(request);
    }
    ServletUtils.setCommonResponseHttpHeaders(response, isXhrRequest);
  }

  static boolean loginHandler(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (! isLoginTarget(target)) {
      return false;
    }

    if (isPageRequest(request)) {
      sendLoginForm(request, response);
    } else {
      ServletUtils.sendResponseError(response, HttpServletResponse.SC_UNAUTHORIZED, "Access denied. Please login.");
    }
    return true;
  }

  private static void sendLoginForm(HttpServletRequest request, HttpServletResponse response) {
    final String uri = ServletUtils.getDecodedUri(request);
    try {
      byte[] bytes;
      try (InputStream resource = water.init.JarHash.getResource2("/login.html")) {
        if (resource == null) {
          throw new IllegalStateException("Login form not found");
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        water.util.FileUtils.copyStream(resource, baos, 2048);
        bytes = baos.toByteArray();
      }
      response.setContentType(RequestServer.MIME_HTML);
      response.setContentLength(bytes.length);
      ServletUtils.setResponseStatus(response, HttpServletResponse.SC_OK);
      final OutputStream os = response.getOutputStream();
      water.util.FileUtils.copyStream(new ByteArrayInputStream(bytes), os, 2048);
      // TODO: this whole method can be replaced with just:
      // org.apache.commons.io.IOUtils.copy( water.init.JarHash.getResource2("/login.html"), os);
      // but it needs to be properly tested
    } catch (Exception e) {
      ServletUtils.sendErrorResponse(response, e, uri);
    } finally {
      ServletUtils.logRequest("GET", request, response);
    }
  }

  private static boolean isPageRequest(HttpServletRequest request) {
    String accept = request.getHeader("Accept");
    return (accept != null) && accept.contains(RequestServer.MIME_HTML);
  }

  private static boolean isLoginTarget(String target) {
    return target.equals("/login") || target.equals("/loginError");
  }

}
