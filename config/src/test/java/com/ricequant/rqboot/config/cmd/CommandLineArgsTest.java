package com.ricequant.rqboot.config.cmd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandLineArgsTest {

  @Test
  void defaultOptionsIncludeManagementDefaults() {
    CommandLineArgs args = new CommandLineArgs();
    args.withDefaultOptions();

    CommandLineParser parser = new CommandLineParser("test-app");
    OptionMap optionMap = parser.parse(new String[0], args);

    assertEquals("0.0.0.0", optionMap.getValue(RicemapDefaultArgs.MgmtHost));
    assertEquals("25000", optionMap.getValue(RicemapDefaultArgs.MgmtPort));
    assertEquals("Ricequant123", optionMap.getValue(RicemapDefaultArgs.MgmtToken));
  }
}
