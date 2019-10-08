package com.ricequant.rqboot.lang;

/**
 * @author chenfeng
 */
public class ObjectHelper {

  public static boolean isEqualTo(Object o1, Object o2) {
    if (o1 == null && o2 == null) {
      return true;
    }

    if (o1 != null) {
      return o1.equals(o2);
    }

    return false;
  }
}
