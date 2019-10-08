package com.ricequant.rqboot.jmx.shared_resource.registry;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenfeng
 */
public class JmxCommand implements Serializable {

  private final String iName;

  private final JmxReturnValue iReturnValue;

  private final List<JmxArg> iArgs;

  private final String iObjectName;

  private final int iHashCode;

  private final String iDescription;

  private final int iStatus;

  public JmxCommand(String objectName, String name, String description, JmxType returnValueType, List<JmxArg> args,
          int status) {
    iObjectName = objectName;
    iName = name;
    iDescription = description;
    iStatus = status;
    iReturnValue = new JmxReturnValue(returnValueType);
    iArgs = new ArrayList<>();
    if (args != null)
      iArgs.addAll(args);

    HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
    iHashCode =
            hashCodeBuilder.append(iObjectName).append(iName).append(iDescription).append(iReturnValue).append(iArgs)
                    .append(iStatus).toHashCode();
  }

  public static JmxCommand operation(String objectName, String name, String description, JmxType returnValueType,
          List<JmxArg> args) {
    return new JmxCommand(objectName, name, description, returnValueType, args, 0);
  }

  public static JmxCommand attribute(String objectName, String name, String description, JmxType returnValueType,
          boolean readable, boolean writable) {
    return new JmxCommand(objectName, name, description, returnValueType, null,
            (readable ? 1 : 0) | (writable ? 2 : 0));
  }

  public String getName() {
    return iName;
  }

  public String getObjectName() {
    return iObjectName;
  }

  /**
   * Detached means the command is detached from an object when doing hash. It also erases the types of arguments, only
   * distinguishes the length.
   *
   * @return a hashable object wrapping the command without having the "objectName" information in hashCode or equals
   */
  public JmxCommandFeature getFeature() {
    return new JmxCommandFeature(iName, iArgs.size());
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;

    if (super.equals(that))
      return true;

    if (!(that instanceof JmxCommand))
      return false;

    JmxCommand o = (JmxCommand) that;

    if (!StringUtils.equals(iObjectName, o.iObjectName))
      return false;

    if (!StringUtils.equals(iDescription, o.iDescription))
      return false;

    if (iStatus != o.iStatus)
      return false;

    if (!StringUtils.equals(iName, o.iName))
      return false;

    if (!ObjectUtils.equals(iReturnValue, o.iReturnValue))
      return false;

    if (!ObjectUtils.equals(iArgs, o.iArgs))
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    return iHashCode;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(iName).append('\t');

    if (iArgs.size() > 0) {
      for (JmxArg arg : iArgs) {
        sb.append("<").append(arg.getName()).append(", ").append(arg.getType()).append("> ");
      }
    }
    return sb.toString();
  }

  public JmxReturnValue getReturnValue() {
    return iReturnValue;
  }

  public List<JmxArg> getArgs() {
    return iArgs;
  }

  public boolean isAttribute() {
    return iStatus != 0;
  }

  public boolean isReadable() {
    return (iStatus & 1) != 0;
  }

  public boolean isWritable() {
    return (iStatus & 2) != 0;
  }
}
