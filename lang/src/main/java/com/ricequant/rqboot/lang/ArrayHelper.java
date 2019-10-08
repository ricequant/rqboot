package com.ricequant.rqboot.lang;

import java.lang.reflect.Array;

/**
 * @author chenfeng
 */
public class ArrayHelper {

  @SuppressWarnings("unchecked")
  public static <T> T[] extend(T[] original, T... more) {
    T[] ret = (T[]) Array.newInstance(original.getClass().getComponentType(), original.length + more.length);
    System.arraycopy(original, 0, ret, 0, original.length);
    System.arraycopy(more, 0, ret, original.length, more.length);
    return ret;
  }
}
