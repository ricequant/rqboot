package com.ricequant.rqboot.lang;

/**
 * @author chenfeng
 */
public class NameHelper {

  public static String upperCaseFirstLetter(String str) {
    return Character.toUpperCase(str.charAt(0)) + str.substring(1);
  }

  public static String lowerCaseFirstLetter(String str) {
    return Character.toLowerCase(str.charAt(0)) + str.substring(1);
  }

  public static String camelCaseToHyphen(String str) {
    String regex = "([a-z])([A-Z])";
    String replacement = "$1-$2";
    return str.replaceAll(regex, replacement).toLowerCase();
  }
}
