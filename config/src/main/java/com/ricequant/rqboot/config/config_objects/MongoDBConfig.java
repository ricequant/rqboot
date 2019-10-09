package com.ricequant.rqboot.config.config_objects;


import com.ricemap.utilities.config.MongoDBSecondaryType;
import com.ricemap.utilities.config.MongoDBType;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenfeng
 */
public class MongoDBConfig {

  private final InetSocketAddress iHost;

  private final List<InetSocketAddress> iSecondaries = new ArrayList<>();

  private final String iDatabaseName;

  private final String iAuthSource;

  private final String iUser;

  private final String iPass;

  private String iCollection;

  private int iMaxConnections = 10;

  MongoDBConfig(String configURLString) throws MalformedURLException {
    URI uri = URI.create(configURLString);
    String authAndUrl = uri.getAuthority();

    String[] authorityParts = authAndUrl.split("@");
    String hostString;
    if (authorityParts.length == 1)
      hostString = authorityParts[0];
    else
      hostString = authorityParts[1];

    String[] hosts = hostString.split(",");
    iHost = socketAddressFromString(hosts[0]);

    if (hosts.length > 1) {
      for (int i = 1; i < hosts.length; i++) {
        String[] addrParts = hosts[i].split(":");
        iSecondaries.add(new InetSocketAddress(addrParts[0], Integer.parseInt(addrParts[1])));
      }
    }

    String dbPath = uri.getPath();
    String[] pathParts = dbPath.split("/");
    if (pathParts.length == 2) {
      iAuthSource = "admin";
      iDatabaseName = pathParts[1];
    }
    else if (pathParts.length == 3) {
      iAuthSource = pathParts[1];
      iDatabaseName = pathParts[2];
    }
    else {
      throw new MalformedURLException("Unable to parse database name");
    }

    if (authorityParts.length == 1) {
      iUser = null;
      iPass = null;
    }
    else {
      String authentication = authorityParts[0];
      String[] authParts = authentication.split(":");
      if (authParts.length != 2)
        throw new MalformedURLException("Unable to parse username and password");

      iUser = authParts[0];
      iPass = authParts[1];
    }

    if (uri.getQuery() != null) {
      String[] queryParts = StringUtils.split(uri.getQuery(), '&');
      for (String queryPart : queryParts) {
        String[] params = StringUtils.split(queryPart, '=');
        switch (params[0]) {
          case "collection":
            iCollection = params[1];
            break;
        }
      }
    }
  }

  MongoDBConfig(MongoDBType type) {
    iHost = new InetSocketAddress(type.getHost(), type.getPort());

    for (MongoDBSecondaryType secondary : type.getSecondary()) {
      iSecondaries.add(new InetSocketAddress(secondary.getName(), secondary.getPort()));
    }

    iDatabaseName = type.getName();
    iAuthSource = type.getAuthSource();
    iUser = type.getUsername();
    iPass = type.getPassword();
    iCollection = type.getCollection();
  }

  private static InetSocketAddress socketAddressFromString(String string) {
    String[] parts = string.split(":");
    if (parts.length == 1)
      return new InetSocketAddress(string, 27017);
    return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
  }

  public static MongoDBConfig fromURLString(String urlString) throws MalformedURLException {
    return new MongoDBConfig(urlString);
  }

  public static MongoDBConfig fromConfigType(MongoDBType type) {
    return new MongoDBConfig(type);
  }

  public String host() {
    return iHost.getHostName();
  }

  public int port() {
    return iHost.getPort();
  }

  public String username() {
    return iUser;
  }

  public char[] password() {
    return iPass.toCharArray();
  }

  public String passwordString() {
    return iPass;
  }

  public String databaseName() {
    return iDatabaseName;
  }

  public boolean needAuthentication() {
    return iUser != null;
  }

  public String authSource() {
    return iAuthSource;
  }

  public void maxConnections(int max) {
    iMaxConnections = max;
  }

  public int maxConnections() {
    return iMaxConnections;
  }

  public String collection() {
    return iCollection;
  }

  public List<InetSocketAddress> secondaries() {
    return iSecondaries;
  }

  /**
   * This connection string is not standard mongodb connection string, it shall only be parsed by this class
   *
   * @return the rq connection string
   */
  public String toConnectionString() {
    StringBuilder sb = new StringBuilder("mongodb://");
    if (iUser != null) {
      sb.append(iUser);
      if (iPass != null)
        sb.append(":").append(iPass);
      sb.append("@");
    }
    sb.append(iHost.getHostName()).append(":").append(iHost.getPort());

    for (InetSocketAddress secondary : iSecondaries)
      sb.append(",").append(secondary.getHostName()).append(":").append(secondary.getPort());

    if (iAuthSource != null && !"admin".equals(iAuthSource))
      sb.append("/").append(iAuthSource);

    sb.append("/").append(iDatabaseName);

    if (iCollection != null)
      sb.append("?");

    if (iCollection != null)
      sb.append("collection=").append(iCollection);

    return sb.toString();
  }

  public String toMongoStandardConnectionString() {
    StringBuilder sb = new StringBuilder("mongodb://");
    if (iUser != null) {
      sb.append(iUser);
      if (iPass != null)
        sb.append(":").append(iPass);
      sb.append("@");
    }
    sb.append(iHost.getHostName()).append(":").append(iHost.getPort());

    for (InetSocketAddress secondary : iSecondaries)
      sb.append(",").append(secondary.getHostName()).append(":").append(secondary.getPort());

    sb.append("/").append(iDatabaseName);

    if (iAuthSource != null && !iAuthSource.equals(iDatabaseName) || iCollection != null)
      sb.append("?");

    boolean printAnd = false;

    if (iAuthSource != null && !iAuthSource.equals(iDatabaseName)) {
      sb.append("authSource=").append(iAuthSource);
      printAnd = true;
    }

    if (iCollection != null) {
      if (printAnd)
        sb.append("&");
      sb.append("collection=").append(iCollection);
      printAnd = true;
    }

    return sb.toString();
  }
}
