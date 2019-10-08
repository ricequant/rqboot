package com.ricequant.rqboot.jmx.server.sample;


import com.ricequant.rqboot.jmx.shared_resource.annotations.JmxBean;
import com.ricequant.rqboot.jmx.shared_resource.annotations.JmxMethod;
import com.ricequant.rqboot.jmx.shared_resource.annotations.JmxParam;

/**
 * @author chenfeng
 */

// the description of the bean
@JmxBean("bean only for testing")
public class BeanSample {

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

  // the setter for property "count"
  // the setter must follow the naming convention: void setSomething(ValueType value)
  @JmxMethod("set the count")
  public void setCount(int count) {
    this.count = count;
  }

  // this is another kind of getter - the boolean getter.
  // it is also an getter but following another convention: boolean isSomething();
  // there are only two kinds of getters.
  @JmxMethod("check if counter is zero")
  public boolean isCountZero() {
    return count == 0;
  }

  // this is an operation.
  // an operation can be any methods, except those follow property conventions.
  @JmxMethod("increase count")
  public void increaseCount(
          // this JmxParam annotation is a must for an operation,
          // because the parameter name is not visible to reflection, thus not available to jmx clients
          // the string in "value" will represent the name, the description is optional.
          // so it can also be @JmxParam("addr")
          @JmxParam(value = "addr", description = "number to be increased") int adder) {
    count += adder;
  }

  @JmxMethod("increase count")
  public void increaseCount(
          // this JmxParam annotation is a must for an operation,
          // because the parameter name is not visible to reflection, thus not available to jmx clients
          // the string in "value" will represent the name, the description is optional.
          // so it can also be @JmxParam("addr")
          @JmxParam(value = "addr", description = "number to be increased") long adder) {
    count += adder;
  }

  // another operation with no arguments
  @JmxMethod("increase count by one")
  public void increaseCount() {
    count++;
  }

}
