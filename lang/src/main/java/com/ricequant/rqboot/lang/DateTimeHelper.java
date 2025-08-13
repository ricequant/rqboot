package com.ricequant.rqboot.lang;

import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author kain
 */
public class DateTimeHelper {

  private static final long cBeginMicro;

  private static final long cMicroDiff;

  private final static ThreadLocal<Calendar> CALENDAR = ThreadLocal.withInitial(() -> {
    Calendar c = Calendar.getInstance();
    c.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    return c;
  });

  static {
    long curNano = System.nanoTime();
    for (int i = 0; i < 1000; i++) {
      System.currentTimeMillis();
    }
    long milliCost = (System.nanoTime() - curNano) / 1000;

    cBeginMicro = System.currentTimeMillis() * 1000;
    cMicroDiff = (System.nanoTime() - milliCost) / 1000;
  }

  public static long getCurrentMicros() {
    return cBeginMicro + (System.nanoTime() / 1000 - cMicroDiff);
  }

  public static long getCurrentReadable() {
    return toReadableTimestamp(getCurrentMicros());
  }

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

  public static String intDateToString(int date, String separator) {
    int day = date % 100;
    int month = date / 100 % 100;
    int year = date / 10000;

    return year + separator + month + separator + day;
  }

  public static String toPythonDateTime(int date, int time) {
    String dateStr = DateTimeHelper.intDateToString(date, "-");
    String timeStr = DateTimeHelper.intTimeToString(time);
    return dateStr + " " + timeStr;
  }

  public static String intTimeToString(int time) {
    int seconds = time % 100;
    int minutes = time / 100 % 100;
    int hours = time / 10000;

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
    Calendar now = CALENDAR.get();
    now.setTimeInMillis(System.currentTimeMillis());
    return currTimeInt(0);
  }

  public static int currTimeInt(int offset) {
    Calendar cal = CALENDAR.get();
    cal.setTimeInMillis(System.currentTimeMillis());
    cal.add(Calendar.SECOND, offset);
    return cal.get(Calendar.HOUR_OF_DAY) * 10000 + cal.get(Calendar.MINUTE) * 100 + cal.get(Calendar.SECOND);
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
   * @param date Market date, valid values: YYYYMMDD, in which YYYY=0000-9999, MM=01-12, DD=01-31
   * @return millis from 1970.1.1
   */
  public static long dateToMillis(int date) {
    int year = date / 10000;
    date = date % 10000;

    int month = date / 100;
    int day = date % 100;

    LocalDate startDay = LocalDate.of(1970, 1, 1);
    LocalDate endDay = LocalDate.of(year, month, day);

    long daysDiff = ChronoUnit.DAYS.between(startDay, endDay);

    return daysDiff * 24 * 60 * 60 * 1000;
  }

  /**
   * @param time Market data entry's time, valid values; hhmmssmmm, in which hh=0-23, mm=00-59, ss=00-59, mmm=000-999
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
   * @param date Market date, valid values: YYYYMMDD, in which YYYY=0000-9999, MM=01-12, DD=01-31
   * @param time Market data entry's time, valid values; hhmmssmmm, in which hh=0-23, mm=00-59, ss=00-59, mmm=000-999
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

  public static int getIntDateFromRQTimestamp(long ts) {
    Calendar c = CALENDAR.get();
    c.setTimeInMillis(ts / 1000);
    int year = c.get(Calendar.YEAR);
    int month = c.get(Calendar.MONTH) + 1;
    int day = c.get(Calendar.DAY_OF_MONTH);

    return year * 10000 + month * 100 + day;
  }

  public static int countMinutes(long datetime) {
    int hours = (int) ((datetime % 1000000000) / 10000000);
    int minutes = (int) ((datetime % 10000000) / 100000);

    return hours * 60 + minutes;
  }

  public static int minutesCountToTime(int minutesCount) {
    return minutesCount / 60 * 100 + minutesCount % 60;
  }

  public static int getSeconds(long datetime) {
    return (int) (datetime % 100000) / 1000;
  }

  public static long toRQEpochTimestamp(long readableTimestamp) {
    if (readableTimestamp == 0)
      return 0;

    if (readableTimestamp < 9999999999999999L)
      readableTimestamp *= 10;

    CALENDAR.get().clear();
    // YYYYMMDDHHMMSSsss
    int year = (int) (readableTimestamp / 10000000000000L);
    int month = (int) (readableTimestamp % 10000000000000L / 100000000000L);
    int day = (int) (readableTimestamp % 100000000000L / 1000000000L);
    int hour = (int) (readableTimestamp % 1000000000L / 10000000L);
    int min = (int) (readableTimestamp % 10000000L / 100000L);
    int sec = (int) (readableTimestamp % 100000L / 1000L);
    int milli = (int) (readableTimestamp % 1000L);

    CALENDAR.get().set(year, month - 1, day, hour, min, sec);
    long ret = (CALENDAR.get().getTimeInMillis() + milli) * 1000;
    if (ret < 0) {
      throw new IllegalArgumentException("input timestamp is not in readable format: " + readableTimestamp);
    }
    return ret;
  }

  public static long getTime(long datetime) {
    return datetime % 1000000000;
  }

  public static int getDate(long datetime) {
    return (int) (datetime / 1000000000L);
  }


  public static long toReadableTimestamp(long rqTimestamp) {
    long ret = rqTimestamp / 1000;
    Calendar c = CALENDAR.get();
    c.setTimeInMillis(ret);
    return c.get(Calendar.YEAR) * 10000000000000L + (1 + c.get(Calendar.MONTH)) * 100000000000L
            + c.get(Calendar.DAY_OF_MONTH) * 1000000000L + c.get(Calendar.HOUR_OF_DAY) * 10000000L
            + c.get(Calendar.MINUTE) * 100000 + c.get(Calendar.SECOND) * 1000 + c.get(Calendar.MILLISECOND);
  }

  public static long stripDateAndCountMicros(long readableTimestamp) {
    return microCounts(stripDate(readableTimestamp) * 1000);
  }

  public static long stripDate(long readableTimestamp) {
    return readableTimestamp % 1000000000L;
  }

  public static long microCounts(long timestamp) {
    long seconds = timestamp / (1000 * 1000);
    long sec = seconds % 100;
    long min = seconds / 100 % 100;
    long hour = seconds / 10000;

    return (hour * 3600 + min * 60 + sec) * 1000 * 1000 + timestamp % (1000 * 1000);
  }

  public static long secondCounts(int intTime) {
    int min = intTime / 100 % 100;
    int hour = intTime / 10000;
    return hour * 3600 + min * 60 + intTime % 100;
  }

  public static int rqEpochToDate(long rqEpoch) {
    return getDate(toReadableTimestamp(rqEpoch));
  }

  public static LocalDateTime dateTimeLongToLocalDateTime(long datetime) {
    int year = (int) (datetime / 10000000000000L);
    int month = (int) (datetime / 100000000000L % 100) - 1;
    int day = (int) (datetime / 1000000000L % 100);
    int hour = (int) (datetime / 10000000L % 100);
    int minute = (int) (datetime / 100000L % 100);
    int second = (int) (datetime / 1000L % 100);
    return LocalDateTime.of(year, month, day, hour, minute, second);
  }

  public static long prependDateAndGetExchangeTimestamp(int date, long currentMicros) {
    long millis = currentMicros / 1000;
    long seconds = millis / 1000 % 60;
    long minutes = millis / 1000 / 60 % 60;
    long hours = millis / 1000 / 60 / 60;
    millis = millis % 1000;
    return hours * 10000000 + minutes * 100000 + seconds * 1000 + millis + date * 1000000000L;
  }

  public static long dateToEpoch(int date) {
    Calendar c = Calendar.getInstance();
    int year = date / 10000;
    int month = date / 100 % 100;
    int day = date % 100;
    c.clear();
    c.set(Calendar.YEAR, year);
    c.set(Calendar.MONTH, month - 1);
    c.set(Calendar.DAY_OF_MONTH, day);
    return c.getTimeInMillis();
  }

  public static long toRQTimestamp(int year, int month, int day, int hour, int minute, int second, int milli) {
    CALENDAR.get().set(year, month - 1, day, hour, minute, second);
    long ret = (CALENDAR.get().getTimeInMillis() + milli) * 1000;
    if (ret < 0) {
      throw new IllegalArgumentException(
              "input is unreasonable: " + year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second
                      + "." + milli);
    }
    return ret;
  }
}
