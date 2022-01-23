package com.ricequant.rqboot.boot;


import com.ricequant.rqboot.config.cmd.CommandLineArgs;
import com.ricequant.rqboot.config.cmd.OptionMap;
import com.ricequant.rqboot.jmx.server.IJmxBeanRegistry;

/**
 * @author chenfeng
 */
public interface IApplication {

  default boolean withDefaultArgs() {
    return false;
  }

  String getAppName();

  void customizeArgs(CommandLineArgs definedArgs);

  void init(OptionMap optionMap, String[] args);

  void start(IJmxBeanRegistry beanRegistry) throws Exception;

  void tearDown();
}
