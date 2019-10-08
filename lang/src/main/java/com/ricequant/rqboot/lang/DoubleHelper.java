package com.ricequant.rqboot.lang;

/**
 * @author chenfeng
 */
@Deprecated
public class DoubleHelper {

  private static final double DEFAULT_PRECISION = 0.00000005;

  public static boolean isGreaterThan(double primary, double target, double precision) {
    return !(primary <= target || isEqualTo(primary, target, precision));
  }

  public static boolean isLessThan(double primary, double target, double precision) {
    return !(primary >= target || isEqualTo(primary, target, precision));
  }

  public static boolean isEqualTo(double primary, double target, double precision) {
    if (primary == target) {
      return true;
    }
    else if (primary - target <= precision && primary - target >= -precision) {
      return true;
    }

    return false;
  }

  public static boolean isGreaterThan(double primary, double target) {
    return isGreaterThan(primary, target, DEFAULT_PRECISION);
  }

  public static boolean isLessThan(double primary, double target) {
    return isLessThan(primary, target, DEFAULT_PRECISION);
  }

  public static boolean isGreaterThanOrEqualTo(double primary, double target) {
    return isGreaterThanOrEqualTo(primary, target, DEFAULT_PRECISION);
  }

  public static boolean isGreaterThanOrEqualTo(double primary, double target, double precision) {
    return !isLessThan(primary, target, precision);
  }

  public static boolean isLessThanOrEqualTo(double primary, double target) {
    return isLessThanOrEqualTo(primary, target, DEFAULT_PRECISION);
  }

  public static boolean isLessThanOrEqualTo(double primary, double target, double precision) {
    return !isGreaterThan(primary, target, precision);
  }

  public static boolean isEqualTo(double primary, double target) {
    return isEqualTo(primary, target, DEFAULT_PRECISION);
  }

  public static double roundDecimals(double number, int decimals) {
    if (decimals < 0)
      throw new IllegalArgumentException();

    long factor = (long) Math.pow(10, decimals);
    number = number * factor;
    long tmp = Math.round(number);
    return (double) tmp / factor;
  }
}
