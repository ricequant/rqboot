package com.ricequant.rqboot.jmx.server;

/**
 * @author chenfeng
 */
public class JmxServerException extends Exception {

  public JmxServerException(String msg) {
    super(msg);
  }

  public JmxServerException(Throwable t) {
    super(t);
  }

  public JmxServerException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
