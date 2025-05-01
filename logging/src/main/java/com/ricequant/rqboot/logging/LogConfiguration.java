package com.ricequant.rqboot.logging;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * @author chenfeng
 */
public class LogConfiguration {

  private final Map<Integer, Set<String>> iLevelToLoggerMap = new TreeMap<>();

  public LogConfiguration(String debugLevel, String logFilePath) {
    try {
      if (logFilePath != null) {
        redirectOutputToFile(logFilePath);
      }
      loadConfigFromResources("base-log-levels.properties");
      loadConfigFromResources("log-levels.properties");

      changeDebugLevel(debugLevel);
    }
    catch (IOException e) {
      System.err.println("Error happened loading resources");
    }
  }

  private void redirectOutputToFile(String logFilePath) {
    try {
      // Check if log file path is specified in command-line arguments
      File logFile;
      // Get the directory where the executable is located
      String workingDir = System.getProperty("user.dir");

      if (logFilePath != null && !logFilePath.isEmpty()) {
        // Use the specified log file path
        logFile = new File(logFilePath);
        if (!StringUtils.equals(logFile.getAbsolutePath(), logFile.getPath())) {
          logFile = new File(workingDir + (logFilePath.startsWith(File.separator) ? logFilePath
                  : (File.separator + logFilePath)));
        }
        System.out.println("Using specified log file: " + logFile.getAbsolutePath());
      }
      else {
        // Create console.log file in the executable's directory
        logFile = new File(workingDir + File.separator + "console.log");
      }

      System.out.println("Redirecting output to: " + logFile.getAbsolutePath());

      // Create a FileOutputStream that appends to the file
      FileOutputStream fos = new FileOutputStream(logFile, true);

      // Create a ComboPrintStream that writes to both file and stdout
      ComboPrintStream comboOut = new ComboPrintStream(fos);

      // Redirect stdout to the combo stream
      System.setOut(comboOut);

      // For stderr, we'll use another ComboPrintStream with the original stderr
      FileOutputStream errFos = new FileOutputStream(logFile, true);
      PrintStream originalErr = System.err;
      ComboPrintStream comboErr = new ComboPrintStream(errFos, originalErr);

      // Redirect stderr to the combo stream
      System.setErr(comboErr);

      System.out.println("\n--- Application started at " + new java.util.Date() + " ---");
    }
    catch (IOException e) {
      System.err.println("Failed to redirect output to console.log: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public static void main(String args[]) throws InterruptedException {
    LogConfiguration config = new LogConfiguration("0", null);
    config.changeDebugLevel("2");

    Thread.sleep(1000);
  }

  private void registerLogLevel(Properties p) {
    for (String key : p.stringPropertyNames()) {
      Integer level = Integer.parseInt(p.getProperty(key, "-1"));

      Set<String> loggerSet = iLevelToLoggerMap.get(level);
      if (loggerSet == null) {
        loggerSet = new HashSet<>();
        iLevelToLoggerMap.put(level, loggerSet);
      }

      loggerSet.add(key);
    }
  }

  public void changeDebugLevel(String level) {

    if ("max".equals(level)) {
      setRootLoggerLevel(Level.DEBUG);
    }
    else if ("perfon".equals(level) || "perfoff".equals(level)) {
      LoggerContext context = (LoggerContext) LogManager.getContext(false);

      final String category = "PERF";
      Level logLevel = "perfon".equals(level) ? Level.DEBUG : Level.INFO;
      Configuration loggerConfig = context.getConfiguration();
      if (!context.hasLogger(category)) {
        loggerConfig.addLogger(category, new LoggerConfig(category, logLevel, false));
      }
      else
        loggerConfig.getLoggerConfig(category).setLevel(logLevel);

      System.out.println("Change performance logging to: " + level);
      context.updateLoggers();
      return;
    }
    else {
      setRootLoggerLevel(Level.INFO);
    }

    int intLevel = "max".equals(level) ? Integer.MAX_VALUE : Integer.parseInt(level);
    LoggerContext context = (LoggerContext) LogManager.getContext(false);
    for (Map.Entry<Integer, Set<String>> entry : iLevelToLoggerMap.entrySet()) {
      for (String category : entry.getValue()) {
        if (entry.getKey() <= intLevel) {
          setLevel(context, category, Level.DEBUG);
        }
        else {
          setLevel(context, category, Level.INFO);
        }
      }
    }
    context.updateLoggers();
    System.out.println("Changed log level to: " + level);
  }

  private static void setLevel(LoggerContext context, String category, Level level) {
    Configuration loggerConfig = context.getConfiguration();
    if (!context.hasLogger(category)) {
      Logger logger = context.getLogger(category);
      loggerConfig.addLogger(category, new LoggerConfig(category, level, false));
      Logger parent = logger.getParent();
      parent.getAppenders().values().forEach(logger::addAppender);
    }
    else {
      loggerConfig.getLoggerConfig(category).setLevel(level);
    }
  }

  public static void setLevel(String category, Level level) {
    LoggerContext context = (LoggerContext) LogManager.getContext(false);
    setLevel(context, category, level);
  }

  private void setRootLoggerLevel(Level level) {
    LoggerContext context = (LoggerContext) LogManager.getContext(false);
    Configuration loggerConfig = context.getConfiguration();
    loggerConfig.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(level);
    loggerConfig.getLoggerConfig("PERF").setLevel(level);
    context.updateLoggers();
  }

  public void loadConfigFromFile(File file) {
    try {
      InputStream is = new FileInputStream(file);
      Properties property = new Properties();
      property.load(is);
      registerLogLevel(property);
    }
    catch (Exception e) {
      throw new RuntimeException("File does not exist");
    }
  }

  private void loadConfigFromResources(String resourceName) throws IOException {
    Enumeration<URL> resources = getClass().getClassLoader().getResources(resourceName);
    while (resources.hasMoreElements()) {
      InputStream resource = resources.nextElement().openStream();
      Properties property = new Properties();
      property.load(resource);
      property.forEach((k, v) -> System.out.println("Log config: " + k + " " + v));
      registerLogLevel(property);
    }
  }


}
