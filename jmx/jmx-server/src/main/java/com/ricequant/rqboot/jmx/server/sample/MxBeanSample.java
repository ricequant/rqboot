package com.ricequant.rqboot.jmx.server.sample;


import com.ricequant.rqboot.jmx.shared_resource.annotations.JmxBean;
import com.ricequant.rqboot.jmx.shared_resource.annotations.JmxMethod;

/**
 * @author chenfeng
 */

// the description of the bean
@JmxBean("mx bean sample only for testing")
public class MxBeanSample implements ISampleMXBean {

  // this variable is private, it will never be exposed.
  // although you may see "count" in the jmx client,
  // it is a result of parsing the method "getCount"
  private int count = 0;

  // a getter of the property "count"
  // a getter must follow the naming convention: ValueType getSomething();
  @JmxMethod("the count")
  public int getCount() {
    return count;
  }


  @Override
  public SomeBean getBean() {
    return new SomeBean("2");
  }

}
