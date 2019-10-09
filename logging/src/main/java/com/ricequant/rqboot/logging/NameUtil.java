package com.ricequant.rqboot.logging;

import org.apache.logging.log4j.util.Strings;

/**
 * @author chenfeng
 */
public class NameUtil {

  public static String getSubName(final String name) {
    if (name.isEmpty()) {
      return null;
    }
    final int i = name.lastIndexOf('.');
    return i > 0 ? name.substring(0, i) : Strings.EMPTY;
  }
}
