package com.ricequant.rqboot.jmx.shared_resource.registry;

import org.apache.commons.lang3.StringUtils;

/**
 * @author chenfeng
 */
public class JmxArg extends JmxReturnValue {

  private final String iName;

  private final String iDescription;

  public JmxArg(String name, String description, JmxType type) {
    super(type);

    iName = name;
    iDescription = description;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<").append(iName).append("|").append(getType()).append("|").append(iDescription).append(">");
    return sb.toString();
  }

  public String getName() {
    return iName;
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;

    if (!(that instanceof JmxArg))
      return false;

    JmxArg o = (JmxArg) that;

    if (!StringUtils.equals(iName, o.iName))
      return false;

    if (!StringUtils.equals(iDescription, o.iDescription))
      return false;

    return super.equals(that);
  }
}
