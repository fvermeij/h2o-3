package ai.h2o.webserver.iface;

public interface H2OProxy {

  int getPort();

  void start(String ip, int port) throws Exception;

}
