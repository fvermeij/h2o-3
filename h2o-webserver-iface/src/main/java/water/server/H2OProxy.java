package water.server;

public interface H2OProxy {

  String getScheme();

  int getPort();

  void start(String ip, int port) throws Exception;

}
