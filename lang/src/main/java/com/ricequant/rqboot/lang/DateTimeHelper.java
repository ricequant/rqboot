package com.ricequant.rqboot.lang;

import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

/**
 * @author kain
 */
public class DateTimeHelper {

  public static boolean isDateFormatValid(int dateTime) {
    try {
      toDateTime(dateTime);
      return true;
    }
    catch (Exception e) {
      return false;
    }
  }

  public static DateTime toDateTime(int dateTime) {
    int day = dateTime % 100;
    int month = dateTime / 100 % 100;
    int year = dateTime / 10000;
    return new DateTime(year, month, day, 0, 0, 0, 0);
  }

  public static Date toDate(int date) {
    return toDateTime(date).toDate();
  }

  public static int toInt(DateTime dateTime) {
    int year = dateTime.getYear();
    int month = dateTime.getMonthOfYear();
    int day = dateTime.getDayOfMonth();
    return year * 10000 + month * 100 + day;
  }

  public static String intDateToString(int date) {
    int day = date % 100;
    int month = date / 100 % 100;
    int year = date / 10000;

    return year + "/" + month + "/" + day;
  }

  public static String intTimeToString(int time) {
    int totalSeconds = time / 1000;
    int seconds = totalSeconds % 100;
    int minutes = totalSeconds / 100 % 100;
    int hours = totalSeconds / 10000;

    StringBuilder sb = new StringBuilder();

    if (hours < 10)
      sb.append("0");

    sb.append(hours).append(":");

    if (minutes < 10)
      sb.append("0");

    sb.append(minutes).append(":");

    if (seconds < 10)
      sb.append("0");

    sb.append(seconds);

    return sb.toString();
  }


  public static int date2Month(int mdEntryDate) {
    return (mdEntryDate / 100) % 100;
  }

  public static int toInt(Date date) {
    return toInt(new DateTime(date));
  }

  public static long makeKey(int entryDate, int entryTime) {
    return ((long) entryDate << 32) | (long) entryTime;
  }

  public static int currTimeInt() {
    Calendar now = Calendar.getInstance();
    return (now.get(Calendar.HOUR_OF_DAY) * 10000 + now.get(Calendar.MINUTE) * 100 + now.get(Calendar.SECOND)) * 1000;
  }

  public static int currMinuteTimeInt() {
    DateTime now = new DateTime();
    return now.getHourOfDay() * 10000000 + now.getMinuteOfHour() * 100000;
  }

  public static int currDateInt() {
    Calendar now = Calendar.getInstance();
    return now.get(Calendar.YEAR) * 10000 + (now.get(Calendar.MONTH) + 1) * 100 + now.get(Calendar.DAY_OF_MONTH);
  }

  public static int lastDate(int daysAgo) {
    DateTime lastDate = new DateTime().minusDays(daysAgo);
    return lastDate.getYear() * 10000 + lastDate.getMonthOfYear() * 100 + lastDate.getDayOfMonth();
  }

  public static int getIntDate(Date date) {
    DateTime dateTime = new DateTime(date);
    int ret = 0;

    ret += dateTime.getYear() * 10000;
    ret += dateTime.getMonthOfYear() * 100;
    ret += dateTime.getDayOfMonth();

    return ret;
  }

  public static int getIntTime(Date date) {
    DateTime dateTime = new DateTime(date);

    System.out.println(dateTime.getHourOfDay());
    System.out.println(dateTime.getMinuteOfHour());
    System.out.println(dateTime.getSecondOfMinute());

    int time = 0;
    time += dateTime.getHourOfDay() * 10000000;
    time += dateTime.getMinuteOfHour() * 100000;
    time += dateTime.getSecondOfMinute() * 1000;
    return time;
  }


  /**
   * @param date
   *         Market date, valid values: YYYYMMDD, in which YYYY=0000-9999, MM=01-12, DD=01-31
   *
   * @return millis from 1970.1.1
   */
  public static long dateToMillis(int date) {
    int year = (int) (date / 10000);
    date = date % 10000;

    int month = (int) (date / 100);
    int day = (int) (date % 100);

    LocalDate startDay = LocalDate.of(1970, 1, 1);
    LocalDate endDay = LocalDate.of(year, month, day);

    long daysDiff = ChronoUnit.DAYS.between(startDay, endDay);

    return daysDiff * 24 * 60 * 60 * 1000;
  }

  /**
   * @param time
   *         Market data entry's time, valid values; hhmmssmmm, in which hh=0-23, mm=00-59, ss=00-59, mmm=000-999
   *
   * @return millis from 1970.1.1
   */
  public static long timeToMillis(int time) {
    long hour = time / (100 * 100 * 1000);
    time = time % (100 * 100 * 1000);

    long minute = time / (100 * 1000);
    time = time % (100 * 1000);

    long second = time / 1000;
    long millisecond = time % 1000;

    return hour * 60 * 60 * 1000 + minute * 60 * 1000 + second * 1000 + millisecond;
  }

  /**
   * @param date
   *         Market date, valid values: YYYYMMDD, in which YYYY=0000-9999, MM=01-12, DD=01-31
   * @param time
   *         Market data entry's time, valid values; hhmmssmmm, in which hh=0-23, mm=00-59, ss=00-59, mmm=000-999
   *
   * @return com.google.protobuf.Timestamp
   */
  public static long toLinuxTimestamp(int date, int time) {
    long dateMillis = dateToMillis(date);
    long timeMillis = timeToMillis(time);
    return dateMillis + timeMillis;
  }

  public static void main(String[] args) {
    System.out.println(currDateInt());
    System.out.println(lastDate(2));
    System.out.println(currMinuteTimeInt());
  }

}
