package com.ricequant.rqboot.logging;


import com.ricequant.rqboot.jmx.shared_resource.annotations.JmxBean;
import com.ricequant.rqboot.jmx.shared_resource.annotations.JmxMethod;
import com.ricequant.rqboot.jmx.shared_resource.annotations.JmxParam;

/**
 * @author chenfeng
 */

@JmxBean("Manages log level changes via JMX")
public class LogLevelJmx {

  private final LogConfiguration iConfig;

  public LogLevelJmx(LogConfiguration config) {
    iConfig = config;
  }

  @JmxMethod("Changes log level to 1...N or max")
  public void dl(@JmxParam(value = "level", description = "1...N or max") String level) {
    iConfig.changeDebugLevel(level);
  }
}
