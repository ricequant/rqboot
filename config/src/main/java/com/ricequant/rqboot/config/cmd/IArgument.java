package com.ricequant.rqboot.config.cmd;

/**
 * @author chenfeng
 */
public interface IArgument {

  String getShortName();

  String getLongName();

  String getDescription();

  boolean isFlag();

  /**
   * When multiple appearances of this argument present, the returned String will be used to separate them when they are
   * got by "String getValue(IArgument)"
   *
   * @return the argument value separator. "null" will result in separating by ";" by default.
   */
  String getMultipleArgSeparator();

  /**
   * Limit the number of appearances of this argument.
   *
   * @return number of the appearance of the argument, "0" means no limit
   */
  int getMaxCount();

  /**
   * Force a checking of the parser. If a required argument is not set, it will print the help and exit
   *
   * @return true if argument is required
   */
  boolean isRequired();

  /**
   * Default value of the argument. If the argument required, this property will be ignored
   *
   * @return null if there is no default value
   */
  String getDefaultValue();
}
