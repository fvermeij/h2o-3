package ai.h2o.webserver.iface;

public interface H2OProxy {

  String getScheme();

  int getPort();

  void start(String ip, int port) throws Exception;

}
