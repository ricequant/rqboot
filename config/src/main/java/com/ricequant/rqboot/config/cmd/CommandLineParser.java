package com.ricequant.rqboot.config.cmd;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author chenfeng
 */
public class CommandLineParser {

  private Logger iLogger = LoggerFactory.getLogger(getClass());

  private String iBanner;

  public CommandLineParser(String appName) {
    iBanner = appName;
  }

  public OptionMap parse(String[] args, CommandLineArgs definedArgs) {
    iLogger.info("Parsing command...");

    OptionMap optionMap = new OptionMap();
    GnuParser parser = new GnuParser();

    try {
      CommandLine line = parser.parse(definedArgs.getOptions(), args);
      if (line.hasOption(RicemapDefaultArgs.Help.getShortName())) {
        displayHelp(definedArgs.getOptions());
        System.exit(0);
      }

      for (Option option : line.getOptions()) {
        if (option.getValue() == null) {
          optionMap.put(option.getOpt(), option.getOpt());
        }
        else {
          optionMap.put(option.getOpt(), option.getValues());
        }
      }

      List<String> validationFailReasons = definedArgs.validate(line);
      if (validationFailReasons.size() != 0) {
        iLogger.error("Command line contains illegal arguments.");
        validationFailReasons.forEach(iLogger::error);
        return null;
      }
    }
    catch (ParseException e) {
      iLogger.error("Error parsing arguments", e);
      displayHelp(definedArgs.getOptions());
      System.exit(0);
    }
    return optionMap;
  }

  private void displayHelp(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    LogPrintWriter lpw = new LogPrintWriter(iLogger);
    try {
      formatter.printHelp(lpw, 120, iBanner, iBanner, options, HelpFormatter.DEFAULT_LEFT_PAD,
              HelpFormatter.DEFAULT_DESC_PAD, null, true);
    }
    finally {
      lpw.flush();
      lpw.close();
    }
  }
}
