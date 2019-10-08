package com.ricequant.rqboot.jmx.shared_resource.registry;

import com.ricequant.rqboot.lang.NameHelper;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;

/**
 * @author chenfeng
 */
public class JmxType implements Serializable {

  private final JmxTypeEnum iBaseType;

  private final boolean iArray;

  JmxType(JmxTypeEnum baseType, boolean isArray) {
    iBaseType = baseType;
    iArray = isArray;
  }

  public static JmxType simpleType(JmxTypeEnum baseType) {
    return new JmxType(baseType, false);
  }

  public static JmxType arrayType(JmxTypeEnum baseType) {
    return new JmxType(baseType, true);
  }

  public JmxTypeEnum getBaseType() {
    return iBaseType;
  }

  public boolean isArray() {
    return iArray;
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;

    if (this == that)
      return true;

    if (!(that instanceof JmxType))
      return false;

    JmxType o = (JmxType) that;

    return ObjectUtils.equals(iBaseType, o.iBaseType) && iArray == o.iArray;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(NameHelper.upperCaseFirstLetter(iBaseType.toString().toLowerCase()));
    if (isArray())
      sb.append("[]");

    return sb.toString();
  }
}
