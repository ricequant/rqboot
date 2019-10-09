package com.ricequant.rqboot.config.cmd;

/**
 * @author chenfeng
 */
public class Argument implements IArgument {

  protected final String iShortName;

  protected final String iLongName;

  protected final String iDescription;

  protected final boolean iFlag;

  protected final String iMultipleArgSeparator;

  protected final int iMaxCount;

  protected final boolean iRequired;

  protected final String iDefaultValue;

  Argument(String shortName, String longName, String description, boolean isFlag, boolean isRequired) {
    iShortName = shortName;
    iLongName = longName;
    iFlag = isFlag;
    iRequired = isRequired;
    iDescription = description;

    iMultipleArgSeparator = null;
    iMaxCount = 0;
    iDefaultValue = null;
  }

  Argument(String shortName, String longName, String description, boolean isFlag, String multipleArgSeparator,
          int maxCount, boolean isRequired, String defaultValue) {
    iShortName = shortName;
    iLongName = longName;
    iDescription = description;
    iFlag = isFlag;
    iMultipleArgSeparator = multipleArgSeparator;
    iMaxCount = maxCount;
    iRequired = isRequired;
    iDefaultValue = defaultValue;
  }

  public static IArgument required(String shortName, String longName, String description) {
    return new Argument(shortName, longName, description, false, true);
  }

  public static IArgument optional(String shortName, String longName, String description, String defaultValue) {
    return new Argument(shortName, longName, description, false, null, 0, false, defaultValue);
  }

  public static IArgument requiredFlag(String shortName, String longName, String description) {
    return new Argument(shortName, longName, description, true, true);
  }

  public static IArgument optionalFlag(String shortName, String longName, String description) {
    return new Argument(shortName, longName, description, true, false);
  }

  public static IArgument requiredMultiValue(String shortName, String longName, String description, int maxCount,
          String multipleArgSeparator) {
    return new Argument(shortName, longName, description, false, multipleArgSeparator, maxCount, true, null);
  }

  /**
   * Returns an arg that will be parsed to multiple values, and it is optional
   *
   * @param shortName
   *         short name of the arg
   * @param longName
   *         long name of the arg
   * @param description
   *         description of the arg
   * @param maxCount
   *         max number of values allowed in the arg. 0 or a negative number means no limit.
   * @param multipleArgSeparator
   *         the separator of the values
   * @param defaultValue
   *         default value of the arg
   *
   * @return an IArgument object
   */
  public static IArgument optionalMultiValue(String shortName, String longName, String description, int maxCount,
          String multipleArgSeparator, String defaultValue) {
    return new Argument(shortName, longName, description, false, multipleArgSeparator, maxCount, false, defaultValue);
  }

  @Override
  public String getShortName() {
    return iShortName;
  }

  @Override
  public String getLongName() {
    return iLongName;
  }

  @Override
  public String getDescription() {
    return iDescription;
  }

  @Override
  public boolean isFlag() {
    return iFlag;
  }

  @Override
  public String getMultipleArgSeparator() {
    return iMultipleArgSeparator;
  }

  @Override
  public int getMaxCount() {
    return iMaxCount;
  }

  @Override
  public boolean isRequired() {
    return iRequired;
  }

  @Override
  public String getDefaultValue() {
    return iDefaultValue;
  }
}
