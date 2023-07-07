package com.ricequant.rqboot.lang;

import java.util.Collection;
import java.util.List;

/**
 * @author kangol
 */
public class TypeHelper {

  public static <T> List<T> conv(List<?> in) {
    return (List<T>) in;
  }

  public static <T> T[] conv(Object[] in) {
    return (T[]) in;
  }

  public static <T> T conv(Object in) {
    return (T) in;
  }

  public static <T> T conv(Object in, Class<T> clazz) {
    return (T) in;
  }

  public static <T> Collection<T> conv(Collection<?> in) {
    return (Collection<T>) in;
  }
}
