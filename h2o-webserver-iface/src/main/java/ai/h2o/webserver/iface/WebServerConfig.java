package ai.h2o.webserver.iface;

import java.util.Map;

public class WebServerConfig {
  public String jks;

  public String jks_pass;

  public boolean hash_login;

  public boolean ldap_login;

  public boolean kerberos_login;

  public boolean pam_login;

  public String login_conf;

  public boolean form_auth;

  public int session_timeout; // parsed value (in minutes)

  public String user_name;

  public String ip;

  public String network;

  public String context_path;

  public Map<String,String> h2oExtendedHeaders;

  // proxy only:

  public int port;

  public int baseport;

  public String web_ip;
}
