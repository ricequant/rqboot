package com.ricequant.rqboot.management.server;

import java.util.Objects;

public class ManagementServerConfig {

  private final String host;

  private final int port;

  private final String token;

  public ManagementServerConfig(String host, int port, String token) {
    this.host = Objects.requireNonNull(host, "host");
    this.port = port;
    this.token = Objects.requireNonNull(token, "token");
  }

  public String host() {
    return host;
  }

  public int port() {
    return port;
  }

  public String token() {
    return token;
  }
}
