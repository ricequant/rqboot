package com.ricequant.rqboot.config.cmd;

import org.apache.commons.cli.*;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author chenfeng
 */
public class CommandLineParser {

  private final String banner;

  public CommandLineParser(String appName) {
    banner = appName;
  }

  public OptionMap parse(String[] args, CommandLineArgs definedArgs) {
    System.out.println("Parsing command...");

    OptionMap optionMap = new OptionMap();
    DefaultParser parser = new DefaultParser();

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
        System.err.println("Command line contains illegal arguments.");
        validationFailReasons.forEach(System.err::println);
        return null;
      }
    }
    catch (ParseException e) {
      System.err.println("Error parsing arguments");
      e.printStackTrace();
      displayHelp(definedArgs.getOptions());
      System.exit(0);
    }
    return optionMap;
  }

  private void displayHelp(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    LogPrintWriter lpw = new LogPrintWriter(LoggerFactory.getLogger(getClass()));
    try {
      formatter.printHelp(lpw, 120, banner, banner, options, HelpFormatter.DEFAULT_LEFT_PAD,
              HelpFormatter.DEFAULT_DESC_PAD, null, true);
    }
    finally {
      lpw.flush();
      lpw.close();
    }
  }
}
