package com.ricequant.rqboot.config;

import com.ricemap.utilities.config.EndPointType;
import com.ricemap.utilities.config.MySQLType;

import java.net.InetSocketAddress;

/**
 * @author chenfeng
 */
public class ConfigHelper {

  private final static String ADDRESS_SEPARATOR = ":";

  public static InetSocketAddress parseEndpoint(EndPointType endPointType) {
    return new InetSocketAddress(endPointType.getHost(), endPointType.getPort());
  }

  public static String toEndpointString(EndPointType endPointType) {
    // in format: ip:port:hbInterval:sendBuffer
    return endPointType.getHost() + ":" + endPointType.getPort() + ":" + (
            endPointType.getHeartBeatIntervalMillis() != null ? endPointType.getHeartBeatIntervalMillis() : "") +
            ":" + endPointType.getSendBuffer();
  }

  public static String toEndpointStringSimple(EndPointType endPointType) {
    // in format: ip:port:hbInterval:sendBuffer
    return endPointType.getHost() + ":" + endPointType.getPort();
  }

  public static EndPointType toEndPointType(String address) {
    String[] parts = address.split(ADDRESS_SEPARATOR);
    EndPointType endPointType = new EndPointType();
    endPointType.setHost(parts[0]);
    endPointType.setPort(Integer.parseInt(parts[1]));
    if (parts.length > 2 && !parts[2].equals(""))
      endPointType.setHeartBeatIntervalMillis(Integer.parseInt(parts[2]));
    if (parts.length > 3)
      endPointType.setSendBuffer(Integer.parseInt(parts[3]));
    return endPointType;
  }

  public static String toMySQLConnectionString(MySQLType mysqlType) {
    if (mysqlType.isEnabled())
      return "mysql://" + (mysqlType.getUsername() != null ? mysqlType.getUsername() : "") + (
              mysqlType.getPassword() != null ? (":" + mysqlType.getPassword()) : "") + (mysqlType.getUsername() != null
              ? "@" : "") + mysqlType.getHost() + ":" + mysqlType.getPort() + "/" + mysqlType.getName();
    else
      return null;
  }
}
