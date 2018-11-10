package ai.h2o.webserver.iface;

public interface H2OServletContainer {

  void start(String ip, int port) throws Exception;

  void stop() throws Exception;

}
