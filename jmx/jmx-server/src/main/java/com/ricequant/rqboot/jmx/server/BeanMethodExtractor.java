package com.ricequant.rqboot.jmx.server;

import com.ricequant.rqboot.jmx.shared_resource.annotations.JmxMethod;
import com.ricequant.rqboot.jmx.shared_resource.annotations.JmxParam;
import com.ricequant.rqboot.jmx.shared_resource.registry.JmxArg;
import com.ricequant.rqboot.lang.MethodHelper;
import com.ricequant.rqboot.lang.NameHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenfeng
 */
class BeanMethodExtractor {

  static Pair<MBeanAttributeInfo[], MBeanOperationInfo[]> extractMethodsInfo(Method[] methods,
          IBeanCommandRegistry registry) {
    List<MBeanAttributeInfo> attrInfoList = new ArrayList<>();
    List<MBeanOperationInfo> opInfoList = new ArrayList<>();

    Map<String, Method[]> attributes = new HashMap<>();
    for (Method method : methods) {
      if (!method.isAnnotationPresent(JmxMethod.class))
        continue;

      JmxMethod methodAnnotation = method.getAnnotation(JmxMethod.class);

      Method getterMethod = MethodHelper.isGetterMethod(method) ? method : null;
      Method setterMethod = MethodHelper.isSetterMethod(method) ? method : null;
      Method operatorMethod = (getterMethod == null && setterMethod == null) ? method : null;

      if (getterMethod != null || setterMethod != null) {
        String attributeName = findAttributeName(method);
        Method[] attributeMethods = attributes.get(attributeName);
        if (attributeMethods == null) {
          attributeMethods = new Method[]{getterMethod, setterMethod};
          attributes.put(attributeName, attributeMethods);
        }
        else {
          if (getterMethod != null)
            attributeMethods[0] = getterMethod;
          if (setterMethod != null)
            attributeMethods[1] = setterMethod;
        }
      }
      else {
        List<JmxArg> argsList = new ArrayList<>();

        MBeanParameterInfo[] params =
                buildMBeanParameterInfo(operatorMethod.getName(), operatorMethod.getParameterTypes(),
                        operatorMethod.getParameterAnnotations(), argsList);

        opInfoList.add(new MBeanOperationInfo(method.getName(), methodAnnotation.value(), params,
                method.getReturnType().getName(), MBeanOperationInfo.UNKNOWN));

        registry.addOperation(method.getName(), methodAnnotation.value(), argsList, method.getReturnType());
      }
    }

    for (Map.Entry<String, Method[]> attribute : attributes.entrySet()) {
      Method getter = attribute.getValue()[0];
      Method setter = attribute.getValue()[1];

      StringBuilder description = new StringBuilder();
      if (getter != null & setter != null)
        description.append("Read & Write: ");
      else if (getter != null)
        description.append("Read Only: ");
      else
        description.append("Write Only: ");

      if (getter != null) {
        description.append(getter.getAnnotation(JmxMethod.class).value()).append(System.lineSeparator());
      }

      if (setter != null) {
        description.append(setter.getAnnotation(JmxMethod.class).value());
      }

      Class<?> attributeType = getter != null ? getter.getReturnType() : setter.getParameterTypes()[0];

      attrInfoList.add(new MBeanAttributeInfo(attribute.getKey(), attributeType.getName(), description.toString(),
              getter != null, setter != null, getter != null && getter.getName().startsWith("is")));

      registry.addAttribute(attribute.getKey(), attributeType, description.toString(), getter != null, setter != null);
    }

    return Pair.of(attrInfoList.toArray(new MBeanAttributeInfo[attrInfoList.size()]),
            opInfoList.toArray(new MBeanOperationInfo[opInfoList.size()]));
  }

  private static String findAttributeName(Method method) {
    String methodName = method.getName();
    if (methodName.startsWith("get") || methodName.startsWith("set"))
      return NameHelper.lowerCaseFirstLetter(methodName.substring(3));
    else if (methodName.startsWith("is"))
      return NameHelper.lowerCaseFirstLetter(methodName.substring(2));
    return method.getName();
  }


  private static MBeanParameterInfo[] buildMBeanParameterInfo(String name, Class<?>[] paramsTypes,
          Annotation[][] paramsAnnotations, List<JmxArg> argsList) {

    MBeanParameterInfo[] mBeanParameters = new MBeanParameterInfo[paramsTypes.length];

    for (int i = 0; i < paramsTypes.length; i++) {
      Annotation[] paramAnnotations = paramsAnnotations[i];
      JmxParam paramAnnotation = null;
      for (Annotation annotation : paramAnnotations) {
        if (annotation instanceof JmxParam) {
          paramAnnotation = (JmxParam) annotation;
          break;
        }
      }

      if (paramAnnotation == null) {
        throw new IllegalArgumentException(
                "A method in a bean who is not a getter or a setter, whose parameters must be annotated by "
                        + JmxParam.class.getCanonicalName() + ". Method name: " + name);
      }

      MBeanParameterInfo parameterInfo =
              new MBeanParameterInfo(paramAnnotation.value(), paramsTypes[i].getName(), paramAnnotation.description());

      argsList.add(new JmxArg(paramAnnotation.value(), paramAnnotation.description(),
              CommandRegistry.parseType(paramsTypes[i])));

      mBeanParameters[i] = parameterInfo;
    }

    return mBeanParameters;
  }
}
