package com.ricequant.rqboot.jmx.shared_resource.registry;

import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;

/**
 * @author chenfeng
 */
public class JmxReturnValue implements Serializable {

  private final JmxType iType;

  public JmxReturnValue(JmxType type) {
    iType = type;
  }

  public JmxType getType() {
    return iType;
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;

    if (!(that instanceof JmxReturnValue))
      return false;

    JmxReturnValue o = (JmxReturnValue) that;
    return ObjectUtils.equals(iType, o.iType);
  }
}
