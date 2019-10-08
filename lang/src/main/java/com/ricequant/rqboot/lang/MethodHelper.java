package com.ricequant.rqboot.lang;

import java.lang.reflect.Method;

/**
 * @author chenfeng
 */
public class MethodHelper {

  public static boolean isGetterMethod(Method method) {
    return (method.getName().startsWith("get") || method.getName().startsWith("is")) && (
            !method.getReturnType().equals(Void.TYPE) && method.getParameterTypes().length == 0);
  }

  public static boolean isSetterMethod(Method method) {
    return method.getName().startsWith("set") && method.getReturnType().equals(Void.TYPE)
            && method.getParameterTypes().length == 1;
  }

  public static Method findGetterMethod(Class<?> objectType, String attributeName) {

    try {
      return objectType.getMethod("get" + NameHelper.upperCaseFirstLetter(attributeName));
    }
    catch (NoSuchMethodException e) {
    }

    try {
      return objectType.getMethod("is" + NameHelper.upperCaseFirstLetter(attributeName));
    }
    catch (NoSuchMethodException e) {
    }

    return null;
  }

  public static Method findSetterMethod(Class<?> objectType, String name, Class<?> attributeType) {

    try {
      return objectType.getMethod("set" + NameHelper.upperCaseFirstLetter(name), attributeType);
    }
    catch (NoSuchMethodException e) {
      return null;
    }
  }
}
