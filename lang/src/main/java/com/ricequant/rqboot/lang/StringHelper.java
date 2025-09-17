package com.ricequant.rqboot.lang;

/**
 * @author kangol
 */
public class StringHelper {

  public static String insert(String str, int index, String insertStr) {
    return str.substring(0, index) + insertStr + str.substring(index);
  }

}
