package com.ricequant.rqboot.jmx.server;

import com.ricequant.rqboot.jmx.shared_resource.annotations.JmxBean;
import org.apache.commons.lang3.tuple.Pair;

import javax.management.*;
import java.lang.reflect.Method;

/**
 * @author chenfeng
 */
class MBeanFactory {

  static DynamicMBean createBean(Object bean, IBeanCommandRegistry registry) {
    if (bean == null)
      throw new NullPointerException("bean cannot be null");

    Class<?> beanType = bean.getClass();

    if (!beanType.isAnnotationPresent(JmxBean.class))
      throw new IllegalArgumentException(
              "Registering " + bean.getClass().getCanonicalName() + " error: bean must be annotated by " + JmxBean.class
                      .getCanonicalName());

    String description = beanType.getAnnotation(JmxBean.class).value();
    Method[] methods = beanType.getMethods();
    Pair<MBeanAttributeInfo[], MBeanOperationInfo[]> beanMethodInfo =
            BeanMethodExtractor.extractMethodsInfo(methods, registry);

    MBeanInfo mBeanInfo =
            new MBeanInfo(beanType.getName(), description, beanMethodInfo.getLeft(), new MBeanConstructorInfo[0],
                    beanMethodInfo.getRight(), new MBeanNotificationInfo[0]);

    return new MBeanImpl(bean, mBeanInfo);
  }

}
