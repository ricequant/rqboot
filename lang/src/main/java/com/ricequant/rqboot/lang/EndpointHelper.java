package com.ricequant.rqboot.lang;

import java.net.InetSocketAddress;

/**
 * @author chenfeng
 */
public class EndpointHelper {

  private static final String IP_PORT_SEPARATOR = ":";

  public static InetSocketAddress convertAddressString(String address) {
    return convertAddressString(address, IP_PORT_SEPARATOR);
  }

  public static InetSocketAddress convertAddressString(String address, String ipPortSeparator) {
    String[] parts = address.split(ipPortSeparator);

    if (parts.length < 2) {
      throw new IllegalArgumentException("Service address must be like ip:port");
    }

    int port = Integer.parseInt(parts[1]);
    String ip = parts[0];

    return new InetSocketAddress(ip, port);
  }

  public static String convertInetSocketAddress(InetSocketAddress addr) {
    return addr.getHostName() + ":" + addr.getPort();
  }
}
