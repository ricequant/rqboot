package com.ricequant.rqboot.config;

/**
 * @author chenfeng
 */
public class CommonProperties {

  public static int communicationRetryDelay() {
    int delay = getIntProperty("rq.communication.retryDelay", "5000");
    return delay < 0 ? 0 : delay;
  }

  public static int communicationRetryTimes() {
    int retries = getIntProperty("rq.communication.retryTimes", "0");
    return retries < 0 ? 0 : retries;
  }

  public static int communicationHeartBeatInterval() {
    int interval = getIntProperty("rq.communication.hbInterval", "5000");
    return interval <= 0 ? Integer.MAX_VALUE : interval;
  }

  public static int communicationLogBufferExceptionTimes() {
    int defaultTimes = 10;
    return getIntProperty("rq.communication.logBufferExceptionTimes", String.valueOf(defaultTimes));
  }

  public static boolean communicationLogConnection() {
    return getBooleanProperty("rq.communication.logConnection", "true");
  }

  private static int getIntProperty(String key, String defaultValue) {
    return Integer.parseInt(System.getProperty(key, defaultValue));
  }

  private static boolean getBooleanProperty(String key, String defaultValue) {
    String booleanString = System.getProperty(key, defaultValue).toLowerCase();
    return "true".equals(booleanString);
  }
}
