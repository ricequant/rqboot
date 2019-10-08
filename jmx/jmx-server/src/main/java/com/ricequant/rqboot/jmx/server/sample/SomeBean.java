package com.ricequant.rqboot.jmx.server.sample;

import java.beans.ConstructorProperties;

/**
 * @author chenfeng
 */
public class SomeBean {

  private String iValue = "1";

  @ConstructorProperties("value")
  public SomeBean(String value) {
    iValue = value;
  }

  public String getValue() {
    return iValue;
  }
}
