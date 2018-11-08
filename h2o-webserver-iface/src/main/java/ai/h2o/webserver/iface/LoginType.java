package ai.h2o.webserver.iface;

/**
 * Supported login methods
 */
public enum LoginType {
  NONE(null),
  HASH(null),
  LDAP("ldaploginmodule"),
  KERBEROS("krb5loginmodule"),
  PAM("pamloginmodule");

  public final String jaasRealm;

  LoginType(String jaasRealm) {
    this.jaasRealm = jaasRealm;
  }

  public boolean isJaas() {
    return jaasRealm != null;
  }
}
