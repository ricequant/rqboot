package com.ricequant.rqboot.management.server;

public class ManagementEndpointsInfo {

  private final boolean httpEnabled;

  private final String httpHost;

  private final int httpPort;

  private final boolean jmxEnabled;

  private final String jmxHost;

  private final Integer jmxPort;

  public ManagementEndpointsInfo(boolean httpEnabled, String httpHost, int httpPort, boolean jmxEnabled, String jmxHost,
          Integer jmxPort) {
    this.httpEnabled = httpEnabled;
    this.httpHost = httpHost;
    this.httpPort = httpPort;
    this.jmxEnabled = jmxEnabled;
    this.jmxHost = jmxHost;
    this.jmxPort = jmxPort;
  }

  public boolean httpEnabled() {
    return httpEnabled;
  }

  public String httpHost() {
    return httpHost;
  }

  public int httpPort() {
    return httpPort;
  }

  public boolean jmxEnabled() {
    return jmxEnabled;
  }

  public String jmxHost() {
    return jmxHost;
  }

  public Integer jmxPort() {
    return jmxPort;
  }
}
