package com.ricequant.rqboot.jmx.shared_resource.registry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author chenfeng
 */
public class JmxCommandFeature {

  private final int iHashCode;

  private final String iName;

  private final int iNumArgs;

  public JmxCommandFeature(String name, int numArgs) {
    iName = name;
    iNumArgs = numArgs;
    HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
    iHashCode = hashCodeBuilder.append(name).append(numArgs).toHashCode();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;

    if (super.equals(that))
      return true;

    if (!(that instanceof JmxCommandFeature))
      return false;

    JmxCommandFeature o = (JmxCommandFeature) that;

    return StringUtils.equals(iName, o.iName) && iNumArgs == o.iNumArgs;
  }

  @Override
  public int hashCode() {
    return iHashCode;
  }

}
