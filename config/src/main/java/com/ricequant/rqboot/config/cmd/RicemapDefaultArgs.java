package com.ricequant.rqboot.config.cmd;

/**
 * @author chenfeng
 */
public enum RicemapDefaultArgs implements IArgument {
  Help("h", "help", true, "display help"),

  DevMode("dev", "dev-mode", true, "Development mode will make the loggers output to the console. "
          + "Default will be production mode and loggers will only output to files"),

  InstanceName("n", "instance-name", false, "Application instance name, will be used in logging, "
          + "configuration file lookup, service registration and other places where identification of the "
          + "application instance is required", null, 1, false, null),

  ConfigFile("c", "conf-file", false, "URI of the configuration file, like http://127.0.0.1/, "
          + "or file://path/to/config/file.xml, or path/to/config/file.xml (default protocol is <file>)"),

  RedirectConsole("rc", "redirect-console", false, "Redirect console output to a log file"),

  TempDir("t", "temp-dir", false, "Temporary file directory"),

  DebugLevel("dl", "debug-level", false, "debug level 0 <omit>, 1, 2 ... max", null, 1, false, "0"),

  JmxHost("jmxhost", "jmx-host", false, "jmx agent host ip", null, 1, false, "127.0.0.1"),

  JmxPort("jmxport", "jmx-port", false, "jmx agent port number", null, 1, false, "0"),

  NoJmx("nojmx", "no-jmx", true, "disable jmx server"),

  Cwd("cwd", "change-working-dir", false, "Change the current working directory to the specified one"),

  IsStatOpen("stat", "stat", true, "whether enable stat"),

  ServiceRegistry("sr", "service-registry", false, "Address of the service registry, like 127.0.0.1:9900"),

  SerializerType("sl", "serializer", false, "which serializer to use, protobuf or rice", null, 1, false, "rice"),

  ;

  private final String iShortName;

  private final String iLongName;

  private final String iDescription;

  private final boolean iFlag;

  private final String iMultipleArgSeparator;

  private final int iMaxCount;

  private final boolean iRequired;

  private final String iDefaultValue;


  RicemapDefaultArgs(String shortName, String longName, boolean isFlag, String description) {
    iShortName = shortName;
    iLongName = longName;
    iFlag = isFlag;
    iDescription = description;

    iMultipleArgSeparator = null;
    iMaxCount = 0;
    iRequired = false;
    iDefaultValue = null;
  }

  RicemapDefaultArgs(String shortName, String longName, boolean isFlag, String description, String multipleArgSeparator,
          int maxCount, boolean required, String defaultValue) {
    iShortName = shortName;
    iLongName = longName;
    iDescription = description;
    iFlag = isFlag;
    iMultipleArgSeparator = multipleArgSeparator;
    iMaxCount = maxCount;
    iRequired = required;
    iDefaultValue = defaultValue;
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
