package com.ricequant.rqboot.boot;


import com.ricequant.rqboot.config.cmd.CommandLineArgs;
import com.ricequant.rqboot.config.cmd.OptionMap;
import com.ricequant.rqboot.jmx.server.IJmxBeanRegistry;
import com.ricequant.rqboot.management.server.ManagementCommandService;

/**
 * @author chenfeng
 */
public interface IApplication {

  String getAppName();

  void customizeArgs(CommandLineArgs definedArgs);

  void init(OptionMap optionMap, String[] args);

  void start(IJmxBeanRegistry beanRegistry, ManagementCommandService managementCommandService) throws Exception;

  void tearDown();
}
