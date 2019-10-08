package com.ricequant.rqboot.jmx.shared_resource.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author chenfeng
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JmxBean {

  /**
   * The description of the bean
   *
   * @return the description string
   */
  String value();
}
