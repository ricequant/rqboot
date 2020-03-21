package com.ricequant.rqboot.boot;


import com.ricequant.rqboot.jmx.shared_resource.annotations.JmxBean;
import com.ricequant.rqboot.jmx.shared_resource.annotations.JmxMethod;

/**
 * @author chenfeng
 */

@JmxBean("VMStatsReporter")
public class VMStatsReporter {

  @JmxMethod("vmstats")
  public String vmstats() {
    double mb = 1024 * 1024;

    //Getting the runtime reference from system
    Runtime runtime = Runtime.getRuntime();

    String ret = "##### Heap utilization statistics [MB] #####" + System.lineSeparator();

    //Print used memory
    ret += ("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb) + System.lineSeparator();

    //Print free memory
    ret += ("Free Memory:" + runtime.freeMemory() / mb) + System.lineSeparator();

    //Print total available memory
    ret += ("Total Memory:" + runtime.totalMemory() / mb) + System.lineSeparator();

    //Print Maximum available memory
    ret += ("Max Memory:" + runtime.maxMemory() / mb) + System.lineSeparator();

    System.out.println(ret);

    return ret;
  }
}
