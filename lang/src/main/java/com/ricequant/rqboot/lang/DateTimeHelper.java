package com.ricequant.rqboot.lang;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utility class for date and time conversions between various formats used in the RiceQuant system.
 *
 * <p>This class handles multiple timestamp and date/time formats:
 * <ul>
 *   <li><b>Int Date Format (YYYYMMDD):</b> Date as integer, e.g., 20231015 for Oct 15, 2023</li>
 *   <li><b>Int Time Format (HHMMSS):</b> Time as integer without milliseconds, e.g., 143020 for 14:30:20</li>
 *   <li><b>Int Time Format (HHMMSSmmm):</b> Time as integer with milliseconds, e.g., 143020500 for 14:30:20.500</li>
 *   <li><b>RQ Timestamp:</b> Microseconds since Unix epoch (1970-01-01 00:00:00 UTC)</li>
 *   <li><b>Readable Timestamp (17-digit):</b> Format YYYYMMDDHHMMSSmmm, e.g., 20231015143020500</li>
 *   <li><b>Unix/Linux Timestamp:</b> Milliseconds since Unix epoch</li>
 * </ul>
 *
 * <p><b>Important Notes:</b>
 * <ul>
 *   <li>All operations use <b>Asia/Shanghai timezone</b> (CST, UTC+8) via ThreadLocal Calendar</li>
 *   <li>The class is thread-safe due to ThreadLocal usage</li>
 *   <li>This class cannot be instantiated (utility class pattern)</li>
 *   <li>Mixing Joda-Time and java.time APIs for historical reasons</li>
 * </ul>
 *
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

  // ==================== Current Time Functions ====================

  /**
   * Gets the current time in microseconds since Unix epoch.
   *
   * <p>This method provides microsecond precision by combining {@code System.currentTimeMillis()}
   * with {@code System.nanoTime()}, adjusting for the cost of time measurement operations.
   *
   * @return current time in microseconds since Unix epoch (1970-01-01 00:00:00 UTC)
   */
  public static long getCurrentMicros() {
    return cBeginMicro + (System.nanoTime() / 1000 - cMicroDiff);
  }

  /**
   * Gets the current time as a readable timestamp in YYYYMMDDHHMMSSmmm format.
   *
   * @return current time as 17-digit readable timestamp (e.g., 20231015143020500)
   * @see #toReadableTimestamp(long)
   */
  public static long getCurrentReadable() {
    return toReadableTimestamp(getCurrentMicros());
  }

  /**
   * Validates whether an integer represents a valid date in YYYYMMDD format.
   *
   * @param dateTime date to validate in YYYYMMDD format (e.g., 20231015)
   * @return {@code true} if the date is valid, {@code false} otherwise
   */
  public static boolean isDateFormatValid(int dateTime) {
    try {
      toDateTime(dateTime);
      return true;
    }
    catch (Exception e) {
      return false;
    }
  }

  // ==================== Integer Date/Time Conversions ====================

  /**
   * Converts an integer date in YYYYMMDD format to Joda-Time DateTime.
   *
   * <p>The time portion is set to midnight (00:00:00.000).
   *
   * @param dateTime date in YYYYMMDD format (e.g., 20231015)
   * @return DateTime object at midnight on the specified date
   * @throws IllegalArgumentException if date components are invalid
   */
  public static DateTime toDateTime(int dateTime) {
    int day = dateTime % 100;
    int month = dateTime / 100 % 100;
    int year = dateTime / 10000;
    return new DateTime(year, month, day, 0, 0, 0, 0);
  }

  /**
   * Converts an integer date in YYYYMMDD format to Java Date.
   *
   * <p>The time portion is set to midnight (00:00:00.000).
   *
   * @param date date in YYYYMMDD format (e.g., 20231015)
   * @return Date object at midnight on the specified date
   */
  public static Date toDate(int date) {
    return toDateTime(date).toDate();
  }

  /**
   * Converts a Joda-Time DateTime to an integer date in YYYYMMDD format.
   *
   * <p>Only the date portion is extracted; time is ignored.
   *
   * @param dateTime DateTime to convert
   * @return date in YYYYMMDD format (e.g., 20231015)
   */
  public static int toInt(DateTime dateTime) {
    int year = dateTime.getYear();
    int month = dateTime.getMonthOfYear();
    int day = dateTime.getDayOfMonth();
    return year * 10000 + month * 100 + day;
  }

  /**
   * Formats an integer date as a string with custom separator.
   *
   * @param date      date in YYYYMMDD format (e.g., 20231015)
   * @param separator separator character(s) to use (e.g., "-", "/", ".")
   * @return formatted date string (e.g., "2023-10-15" with separator "-")
   */
  public static String intDateToString(int date, String separator) {
    int day = date % 100;
    int month = date / 100 % 100;
    int year = date / 10000;

    return year + separator + month + separator + day;
  }

  /**
   * Formats date and time integers as a Python-style datetime string.
   *
   * <p>Produces format "YYYY-MM-DD HH:MM:SS" suitable for Python datetime parsing.
   *
   * @param date date in YYYYMMDD format (e.g., 20231015)
   * @param time time in HHMMSS format (e.g., 143020)
   * @return formatted string (e.g., "2023-10-15 14:30:20")
   */
  public static String toPythonDateTime(int date, int time) {
    String dateStr = DateTimeHelper.intDateToString(date, "-");
    String timeStr = DateTimeHelper.intTimeToString(time);
    return dateStr + " " + timeStr;
  }

  /**
   * Formats an integer time as a string in HH:MM:SS format.
   *
   * @param time time in HHMMSS format (e.g., 143020)
   * @return formatted time string (e.g., "14:30:20")
   */
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

  /**
   * Extracts the month component from an integer date.
   *
   * @param mdEntryDate date in YYYYMMDD format (e.g., 20231015)
   * @return month as integer (1-12)
   */
  public static int date2Month(int mdEntryDate) {
    return (mdEntryDate / 100) % 100;
  }

  /**
   * Converts a Java Date to an integer date in YYYYMMDD format.
   *
   * <p>Only the date portion is extracted; time is ignored.
   *
   * @param date Date to convert
   * @return date in YYYYMMDD format (e.g., 20231015)
   */
  public static int toInt(Date date) {
    return toInt(new DateTime(date));
  }

  /**
   * Combines date and time integers into a single long key.
   *
   * <p>Uses bit shifting to pack both values into a long: date in upper 32 bits, time in lower 32 bits.
   *
   * @param entryDate date in YYYYMMDD format (e.g., 20231015)
   * @param entryTime time in HHMMSSmmm format (e.g., 143020500)
   * @return combined key as long value
   */
  public static long makeKey(int entryDate, int entryTime) {
    return ((long) entryDate << 32) | (long) entryTime;
  }

  /**
   * Gets the current time as an integer in HHMMSS format.
   *
   * <p>Uses Asia/Shanghai timezone.
   *
   * @return current time in HHMMSS format (e.g., 143020 for 14:30:20)
   */
  public static int currTimeInt() {
    Calendar now = CALENDAR.get();
    now.setTimeInMillis(System.currentTimeMillis());
    return currTimeInt(0);
  }

  /**
   * Gets the current time with an offset as an integer in HHMMSS format.
   *
   * <p>Uses Asia/Shanghai timezone.
   *
   * @param offset offset in seconds (positive for future, negative for past)
   * @return time in HHMMSS format (e.g., 143020 for 14:30:20)
   */
  public static int currTimeInt(int offset) {
    Calendar cal = CALENDAR.get();
    cal.setTimeInMillis(System.currentTimeMillis());
    cal.add(Calendar.SECOND, offset);
    return cal.get(Calendar.HOUR_OF_DAY) * 10000 + cal.get(Calendar.MINUTE) * 100 + cal.get(Calendar.SECOND);
  }

  /**
   * Gets the current time as an integer in HHMMSSmmm format (with milliseconds precision).
   *
   * <p>Note: Milliseconds are included in the format (last 5 digits represent MM000).
   * Uses Asia/Shanghai timezone.
   *
   * @return current time in HHMMSSmmm format (e.g., 143020000 for 14:30:20.000)
   */
  public static int currMinuteTimeInt() {
    DateTime now = new DateTime();
    return now.getHourOfDay() * 10000000 + now.getMinuteOfHour() * 100000;
  }

  /**
   * Gets the current date as an integer in YYYYMMDD format.
   *
   * <p>Uses system default timezone.
   *
   * @return current date in YYYYMMDD format (e.g., 20231015 for Oct 15, 2023)
   */
  public static int currDateInt() {
    Calendar now = Calendar.getInstance();
    return now.get(Calendar.YEAR) * 10000 + (now.get(Calendar.MONTH) + 1) * 100 + now.get(Calendar.DAY_OF_MONTH);
  }

  /**
   * Gets a past date as an integer in YYYYMMDD format.
   *
   * <p>Calculates the date that is {@code daysAgo} days before today.
   *
   * @param daysAgo number of days in the past (e.g., 2 for day before yesterday)
   * @return date in YYYYMMDD format
   */
  public static int lastDate(int daysAgo) {
    DateTime lastDate = new DateTime().minusDays(daysAgo);
    return lastDate.getYear() * 10000 + lastDate.getMonthOfYear() * 100 + lastDate.getDayOfMonth();
  }

  /**
   * Converts a Java Date to an integer date in YYYYMMDD format.
   *
   * <p>Only the date portion is extracted; time is ignored.
   * This is functionally equivalent to {@link #toInt(Date)}.
   *
   * @param date Date to convert
   * @return date in YYYYMMDD format (e.g., 20231015)
   */
  public static int getIntDate(Date date) {
    DateTime dateTime = new DateTime(date);
    int ret = 0;

    ret += dateTime.getYear() * 10000;
    ret += dateTime.getMonthOfYear() * 100;
    ret += dateTime.getDayOfMonth();

    return ret;
  }

  /**
   * Converts a Java Date to an integer time in HHMMSSmmm format.
   *
   * <p>Extracts time with millisecond precision.
   *
   * @param date Date to convert
   * @return time in HHMMSSmmm format (e.g., 143020500 for 14:30:20.500)
   */
  public static int getIntTime(Date date) {
    DateTime dateTime = new DateTime(date);
    int time = 0;
    time += dateTime.getHourOfDay() * 10000000;
    time += dateTime.getMinuteOfHour() * 100000;
    time += dateTime.getSecondOfMinute() * 1000;
    return time;
  }


  // ==================== Time Calculation Functions ====================

  /**
   * Converts an integer date to milliseconds since Unix epoch.
   *
   * @param date Market date in YYYYMMDD format (e.g., 20231015), where YYYY=0000-9999, MM=01-12, DD=01-31
   * @return milliseconds since 1970-01-01 00:00:00 UTC
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
   * Converts an integer time to milliseconds (duration from midnight).
   *
   * @param time Market data entry's time in HHMMSSmmm format (e.g., 143020500), where HH=0-23, MM=00-59, SS=00-59,
   *             mmm=000-999
   * @return milliseconds elapsed from midnight (not epoch timestamp)
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
   * Combines integer date and time into a Unix timestamp (milliseconds since epoch).
   *
   * @param date Market date in YYYYMMDD format (e.g., 20231015), where YYYY=0000-9999, MM=01-12, DD=01-31
   * @param time Market data entry's time in HHMMSSmmm format (e.g., 143020500), where HH=0-23, MM=00-59, SS=00-59,
   *             mmm=000-999
   * @return Unix timestamp in milliseconds since epoch
   */
  public static long toLinuxTimestamp(int date, int time) {
    long dateMillis = dateToMillis(date);
    long timeMillis = timeToMillis(time);
    return dateMillis + timeMillis;
  }

  // ==================== RQ Timestamp Operations ====================

  /**
   * Extracts the date component from an RQ timestamp (microseconds since epoch).
   *
   * <p>Uses Asia/Shanghai timezone for extraction.
   *
   * @param ts RQ timestamp in microseconds since Unix epoch
   * @return date in YYYYMMDD format (e.g., 20231015)
   */
  public static int getIntDateFromRQTimestamp(long ts) {
    Calendar c = CALENDAR.get();
    c.setTimeInMillis(ts / 1000);
    int year = c.get(Calendar.YEAR);
    int month = c.get(Calendar.MONTH) + 1;
    int day = c.get(Calendar.DAY_OF_MONTH);

    return year * 10000 + month * 100 + day;
  }

  /**
   * Counts total minutes from a readable timestamp's time component.
   *
   * <p>Extracts hours and minutes from the time portion and returns total minutes since midnight.
   *
   * @param datetime readable timestamp in YYYYMMDDHHMMSSmmm format (e.g., 20231015143020500)
   * @return total minutes since midnight (e.g., 870 for 14:30)
   */
  public static int countMinutes(long datetime) {
    int hours = (int) ((datetime % 1000000000) / 10000000);
    int minutes = (int) ((datetime % 10000000) / 100000);

    return hours * 60 + minutes;
  }

  /**
   * Converts a minute count to an integer time in HHMM00 format.
   *
   * <p>Converts total minutes since midnight to time format (seconds will be 00).
   *
   * @param minutesCount total minutes since midnight (e.g., 870)
   * @return time as integer in HHMM00 format (e.g., 143000 for 870 minutes)
   */
  public static int minutesCountToTime(int minutesCount) {
    return minutesCount / 60 * 100 + minutesCount % 60;
  }

  /**
   * Extracts seconds from a readable timestamp.
   *
   * @param datetime readable timestamp in YYYYMMDDHHMMSSmmm format (e.g., 20231015143020500)
   * @return seconds component (0-59)
   */
  public static int getSeconds(long datetime) {
    return (int) (datetime % 100000) / 1000;
  }

  /**
   * Converts a readable timestamp to RQ epoch timestamp (microseconds since Unix epoch).
   *
   * <p>Uses Asia/Shanghai timezone for conversion.
   * Handles both 16-digit and 17-digit readable formats by appending a zero if needed.
   *
   * @param readableTimestamp timestamp in YYYYMMDDHHMMSSmmm format (e.g., 20231015143020500)
   * @return RQ timestamp in microseconds since Unix epoch, or 0 if input is 0
   * @throws IllegalArgumentException if the timestamp cannot be converted (results in negative value)
   */
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

  // ==================== Readable Timestamp Operations ====================

  /**
   * Extracts the time component from a readable timestamp.
   *
   * <p>Returns time in HHMMSSmmm format by removing the date portion.
   *
   * @param datetime readable timestamp in YYYYMMDDHHMMSSmmm format (e.g., 20231015143020500)
   * @return time component in HHMMSSmmm format (e.g., 143020500)
   */
  public static long getTime(long datetime) {
    return datetime % 1000000000;
  }

  /**
   * Extracts the date component from a readable timestamp.
   *
   * <p>Returns date in YYYYMMDD format by removing the time portion.
   *
   * @param datetime readable timestamp in YYYYMMDDHHMMSSmmm format (e.g., 20231015143020500)
   * @return date component in YYYYMMDD format (e.g., 20231015)
   */
  public static int getDate(long datetime) {
    return (int) (datetime / 1000000000L);
  }

  /**
   * Converts an RQ timestamp (microseconds) to readable timestamp format.
   *
   * <p>Uses Asia/Shanghai timezone for conversion.
   * Produces YYYYMMDDHHMMSSmmm format.
   *
   * @param rqTimestamp RQ timestamp in microseconds since Unix epoch
   * @return readable timestamp in YYYYMMDDHHMMSSmmm format (e.g., 20231015143020500)
   */
  public static long toReadableTimestamp(long rqTimestamp) {
    long ret = rqTimestamp / 1000;
    Calendar c = CALENDAR.get();
    c.setTimeInMillis(ret);
    return c.get(Calendar.YEAR) * 10000000000000L + (1 + c.get(Calendar.MONTH)) * 100000000000L
            + c.get(Calendar.DAY_OF_MONTH) * 1000000000L + c.get(Calendar.HOUR_OF_DAY) * 10000000L
            + c.get(Calendar.MINUTE) * 100000 + c.get(Calendar.SECOND) * 1000 + c.get(Calendar.MILLISECOND);
  }

  /**
   * Strips the date and converts time to microsecond count since midnight.
   *
   * @param readableTimestamp timestamp in YYYYMMDDHHMMSSmmm format (e.g., 20231015143020500)
   * @return microseconds since midnight
   */
  public static long stripDateAndCountMicros(long readableTimestamp) {
    return microCounts(stripDate(readableTimestamp) * 1000);
  }

  /**
   * Strips the date component, keeping only the time portion.
   *
   * @param readableTimestamp timestamp in YYYYMMDDHHMMSSmmm format (e.g., 20231015143020500)
   * @return time component in HHMMSSmmm format (e.g., 143020500)
   */
  public static long stripDate(long readableTimestamp) {
    return readableTimestamp % 1000000000L;
  }

  /**
   * Parses a datetime string and converts to RQ timestamp (microseconds since Unix epoch).
   *
   * <p>Uses Asia/Shanghai timezone for conversion.
   * Expects format "yyyy-MM-dd HH:mm:ss" (e.g., "2023-10-15 14:30:20").
   *
   * @param datetime string in format "yyyy-MM-dd HH:mm:ss"
   * @return RQ timestamp in microseconds since Unix epoch
   */
  public static long toRQTimestamp(String datetime) {
    String[] split = StringUtils.split(datetime, " ");
    String[] dates = StringUtils.split(split[0], "-");
    String[] times = StringUtils.split(split[1], ":");

    int year = Integer.parseInt(dates[0]);
    int month = Integer.parseInt(dates[1]);
    int day = Integer.parseInt(dates[2]);

    int hour = Integer.parseInt(times[0]);
    int min = Integer.parseInt(times[1]);
    int sec = Integer.parseInt(times[2]);

    return toRQTimestamp(year, month, day, hour, min, sec, 0);
  }

  /**
   * Converts a time in HHMMSSmmm format to microsecond count since midnight.
   *
   * <p>Parses the timestamp format and calculates total microseconds elapsed from midnight.
   *
   * @param timestamp time in HHMMSSmmm×1000 format (microseconds representation)
   * @return total microseconds since midnight
   */
  public static long microCounts(long timestamp) {
    long seconds = timestamp / (1000 * 1000);
    long sec = seconds % 100;
    long min = seconds / 100 % 100;
    long hour = seconds / 10000;

    return (hour * 3600 + min * 60 + sec) * 1000 * 1000 + timestamp % (1000 * 1000);
  }

  /**
   * Converts an integer time to second count since midnight.
   *
   * @param intTime time in HHMMSS format (e.g., 143020)
   * @return total seconds since midnight (e.g., 52220 for 14:30:20)
   */
  public static long secondCounts(int intTime) {
    int min = intTime / 100 % 100;
    int hour = intTime / 10000;
    return hour * 3600 + min * 60 + intTime % 100;
  }

  /**
   * Extracts date from an RQ epoch timestamp.
   *
   * @param rqEpoch RQ timestamp in microseconds since Unix epoch
   * @return date in YYYYMMDD format (e.g., 20231015)
   */
  public static int rqEpochToDate(long rqEpoch) {
    return getDate(toReadableTimestamp(rqEpoch));
  }

  /**
   * Converts a readable timestamp (17-digit long) to Java LocalDateTime.
   *
   * <p>Parses YYYYMMDDHHMMSSmmm format into LocalDateTime components.
   * Note: milliseconds component is not included in the LocalDateTime (only seconds precision).
   *
   * @param datetime readable timestamp in YYYYMMDDHHMMSSmmm format (e.g., 20231015143020500)
   * @return LocalDateTime object representing the datetime (without milliseconds)
   */
  public static LocalDateTime dateTimeLongToLocalDateTime(long datetime) {
    int year = (int) (datetime / 10000000000000L);
    int month = (int) (datetime / 100000000000L % 100);
    int day = (int) (datetime / 1000000000L % 100);
    int hour = (int) (datetime / 10000000L % 100);
    int minute = (int) (datetime / 100000L % 100);
    int second = (int) (datetime / 1000L % 100);
    return LocalDateTime.of(year, month, day, hour, minute, second);
  }

  /**
   * Prepends a date to microsecond time and creates a readable timestamp.
   *
   * <p>Combines a date integer with a microsecond time count to produce a readable timestamp.
   *
   * @param date          date in YYYYMMDD format (e.g., 20231015)
   * @param currentMicros microseconds since midnight
   * @return readable timestamp in YYYYMMDDHHMMSSmmm format
   */
  public static long prependDateAndGetExchangeTimestamp(int date, long currentMicros) {
    long millis = currentMicros / 1000;
    long seconds = millis / 1000 % 60;
    long minutes = millis / 1000 / 60 % 60;
    long hours = millis / 1000 / 60 / 60;
    millis = millis % 1000;
    return hours * 10000000 + minutes * 100000 + seconds * 1000 + millis + date * 1000000000L;
  }

  /**
   * Converts an integer date to Unix epoch milliseconds (timestamp at midnight).
   *
   * @param date date in YYYYMMDD format (e.g., 20231015)
   * @return milliseconds since Unix epoch at midnight of the specified date
   */
  public static long dateToEpoch(int date) {
    return dateToEpoch(date, TimeZone.getDefault());
  }

  /**
   * Converts an integer date to Unix epoch milliseconds (timestamp at midnight).
   *
   * @param date date in YYYYMMDD format (e.g., 20231015)
   * @return milliseconds since Unix epoch at midnight of the specified date
   */
  public static long dateToEpoch(int date, TimeZone timeZone) {
    Calendar c = Calendar.getInstance();
    int year = date / 10000;
    int month = date / 100 % 100;
    int day = date % 100;
    c.clear();
    c.setTimeZone(timeZone);
    c.set(Calendar.YEAR, year);
    c.set(Calendar.MONTH, month - 1);
    c.set(Calendar.DAY_OF_MONTH, day);
    return c.getTimeInMillis();
  }

  /**
   * Creates an RQ timestamp from individual datetime components.
   *
   * <p>Uses Asia/Shanghai timezone for conversion.
   *
   * @param year   year (e.g., 2023)
   * @param month  month (1-12)
   * @param day    day of month (1-31)
   * @param hour   hour (0-23)
   * @param minute minute (0-59)
   * @param second second (0-59)
   * @param milli  millisecond (0-999)
   * @return RQ timestamp in microseconds since Unix epoch
   * @throws IllegalArgumentException if the datetime components result in a negative timestamp
   */
  public static long toRQTimestamp(int year, int month, int day, int hour, int minute, int second, int milli) {
    Calendar cal = CALENDAR.get();
    cal.clear(); // Clear all fields to avoid residual values
    cal.set(year, month - 1, day, hour, minute, second);
    long ret = (cal.getTimeInMillis() + milli) * 1000;
    if (ret < 0) {
      throw new IllegalArgumentException(
              "input is unreasonable: " + year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second
                      + "." + milli);
    }
    return ret;
  }

  /**
   * Converts integer date and time to Unix epoch seconds.
   *
   * <p>Uses system default timezone for conversion.
   *
   * @param readableDate date in YYYYMMDD format (e.g., 20231015)
   * @param readableTime time in HHMMSS format (e.g., 143020)
   * @return Unix timestamp in seconds since epoch
   */
  public static long toEpochSecond(int readableDate, int readableTime) {
    int year = readableDate / 10000;
    int month = (readableDate % 10000) / 100;
    int day = readableDate % 100;

    int hour = readableTime / 10000;
    int minute = (readableTime % 10000) / 100;
    int second = readableTime % 100;

    LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute, second);
    return dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
  }

  /**
   * Converts integer date and time to a 17-digit readable timestamp format.
   *
   * <p>Combines YYYYMMDD and HHMMSS formats into YYYYMMDDHHMMSSmmm format.
   * Note: Milliseconds will be 000 since the input time format doesn't include them.
   *
   * @param readableDate date in YYYYMMDD format (e.g., 20231015)
   * @param readableTime time in HHMMSS format (e.g., 143020)
   * @return 17-digit readable timestamp in YYYYMMDDHHMMSSmmm format (e.g., 20231015143020000)
   */
  public static long convertTimeToInt17(int readableDate, int readableTime) {
    int year = readableDate / 10000;
    int month = (readableDate % 10000) / 100;
    int day = readableDate % 100;

    int hour = readableTime / 10000;
    int minute = (readableTime % 10000) / 100;
    int second = readableTime % 100;

    // Format: YYYYMMDDHHMMSSmmm (17 digits)
    return year * 10000000000000L + month * 100000000000L + day * 1000000000L + hour * 10000000L + minute * 100000L
            + second * 1000L;
  }

  /**
   * Checks if an RQ timestamp represents today's date.
   *
   * <p>Extracts the date from the RQ timestamp and compares it with the current date.
   *
   * @param rqTimestamp RQ timestamp in microseconds since Unix epoch
   * @return {@code true} if the timestamp is from today, {@code false} otherwise
   */
  public static boolean isToday(long rqTimestamp) {
    return rqEpochToDate(rqTimestamp) == currDateInt();
  }
}
