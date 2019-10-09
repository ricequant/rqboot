package com.ricequant.rqboot.boot;

import com.ricemap.utilities.config.cmd.CommandLineArgs;
import com.ricemap.utilities.config.cmd.OptionMap;
import com.ricemap.utilities.jmx.server.IJmxBeanRegistry;

/**
 * @author chenfeng
 */
public interface IApplication {

  String getAppName();

  void customizeArgs(CommandLineArgs definedArgs);

  void init(OptionMap optionMap, String[] args);

  void start(IJmxBeanRegistry beanRegistry) throws Exception;

  void tearDown();
}
