package ai.h2o.webserver;

import ai.h2o.webserver.iface.H2OHttpServer;
import ai.h2o.webserver.iface.WebServerConfig;
import water.api.RequestServer;
import water.server.ServletUtils;
import water.util.Log;

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
public class H2OHttpServerImpl implements H2OHttpServer {
  //------------------------------------------------------------------------------------------
  // Object-specific things.
  //------------------------------------------------------------------------------------------
  private static volatile boolean _acceptRequests = false;
  private final WebServerConfig config;

  /**
   * Create bare Jetty object.
   */
  public H2OHttpServerImpl(WebServerConfig config) {
    this.config = config;
  }

  @Override
  public void acceptRequests() {
    _acceptRequests = true;
  }

  @Override
  public boolean authenticationHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!config.loginType.isJaas()) {
      //TODO question: for LoginType.HASH, does this equal not adding this handler at all? if so, it might be better doing it that way
      return false;
    }

    final String loginName = request.getUserPrincipal().getName();
    if (loginName.equals(config.user_name)) {
      return false;
    }
    Log.warn("Login name (" + loginName + ") does not match cluster owner name (" + config.user_name + ")");
    sendUnauthorizedResponse(response, "Login name does not match cluster owner name");
    return true;
  }

  public void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
    ServletUtils.sendResponseError(response, HttpServletResponse.SC_UNAUTHORIZED, message);
  }

  //TODO make this effective in proxy instead of sendUnauthorizedResponse
  public void sendUnauthorizedResponse__Proxy(HttpServletResponse response, String message) throws IOException {
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
  }


  @Override
  public void gateHandler(HttpServletRequest request, HttpServletResponse response) {
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

  @Override
  public boolean loginHandler(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
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

  @Override
  public WebServerConfig getConfig() {
    return config;
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
