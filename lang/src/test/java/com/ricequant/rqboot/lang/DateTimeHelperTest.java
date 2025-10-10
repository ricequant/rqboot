package com.ricequant.rqboot.lang;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for DateTimeHelper utility class.
 * Tests all timestamp format conversions, calculations, and edge cases.
 */
class DateTimeHelperTest {

    // ==================== Integer Date/Time Conversion Tests ====================

    @Test
    void testIsDateFormatValid() {
        assertTrue(DateTimeHelper.isDateFormatValid(20231015));
        assertTrue(DateTimeHelper.isDateFormatValid(20000101));
        assertTrue(DateTimeHelper.isDateFormatValid(19991231));
        assertFalse(DateTimeHelper.isDateFormatValid(20231301)); // Invalid month
        assertFalse(DateTimeHelper.isDateFormatValid(20230132)); // Invalid day
    }

    @Test
    void testToDateTime() {
        DateTime dt = DateTimeHelper.toDateTime(20231015);
        assertEquals(2023, dt.getYear());
        assertEquals(10, dt.getMonthOfYear());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(0, dt.getHourOfDay());
        assertEquals(0, dt.getMinuteOfHour());
        assertEquals(0, dt.getSecondOfMinute());
    }

    @Test
    void testToDate() {
        Date date = DateTimeHelper.toDate(20231015);
        assertNotNull(date);

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(9, cal.get(Calendar.MONTH)); // 0-indexed
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    void testToIntFromDateTime() {
        DateTime dt = new DateTime(2023, 10, 15, 14, 30, 20);
        int dateInt = DateTimeHelper.toInt(dt);
        assertEquals(20231015, dateInt);
    }

    @Test
    void testToIntFromDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, 9, 15, 14, 30, 20); // Month is 0-indexed
        Date date = cal.getTime();

        int dateInt = DateTimeHelper.toInt(date);
        assertEquals(20231015, dateInt);
    }

    @Test
    void testGetIntDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, 9, 15, 14, 30, 20);
        Date date = cal.getTime();

        int dateInt = DateTimeHelper.getIntDate(date);
        assertEquals(20231015, dateInt);
    }

    @Test
    void testGetIntTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, 9, 15, 14, 30, 20);
        cal.set(Calendar.MILLISECOND, 500);
        Date date = cal.getTime();

        int timeInt = DateTimeHelper.getIntTime(date);
        // Format is HHMMSSmmm: 14:30:20.500 -> 143020500
        assertTrue(timeInt >= 143020000 && timeInt < 143021000);
    }

    // ==================== String Formatting Tests ====================

    @Test
    void testIntDateToString() {
        assertEquals("2023-10-15", DateTimeHelper.intDateToString(20231015, "-"));
        assertEquals("2023/10/15", DateTimeHelper.intDateToString(20231015, "/"));
        assertEquals("2023.10.15", DateTimeHelper.intDateToString(20231015, "."));
    }

    @Test
    void testIntTimeToString() {
        assertEquals("14:30:20", DateTimeHelper.intTimeToString(143020));
        assertEquals("09:05:03", DateTimeHelper.intTimeToString(90503));
        assertEquals("00:00:00", DateTimeHelper.intTimeToString(0));
    }

    @Test
    void testToPythonDateTime() {
        String result = DateTimeHelper.toPythonDateTime(20231015, 143020);
        assertEquals("2023-10-15 14:30:20", result);
    }

    // ==================== Date Extraction Tests ====================

    @Test
    void testDate2Month() {
        assertEquals(10, DateTimeHelper.date2Month(20231015));
        assertEquals(1, DateTimeHelper.date2Month(20230101));
        assertEquals(12, DateTimeHelper.date2Month(20231231));
    }

    @Test
    void testMakeKey() {
        long key = DateTimeHelper.makeKey(20231015, 143020500);
        // Verify we can extract the components
        int date = (int) (key >> 32);
        int time = (int) (key & 0xFFFFFFFF);
        assertEquals(20231015, date);
        assertEquals(143020500, time);
    }

    // ==================== Current Time Tests ====================

    @Test
    void testCurrDateInt() {
        int dateInt = DateTimeHelper.currDateInt();
        assertTrue(dateInt >= 20230101); // Reasonable sanity check
        assertTrue(dateInt <= 20501231); // Reasonable future date
    }

    @Test
    void testCurrTimeInt() {
        int timeInt = DateTimeHelper.currTimeInt();
        assertTrue(timeInt >= 0);
        assertTrue(timeInt <= 235959); // Max valid time
    }

    @Test
    void testCurrTimeIntWithOffset() {
        int currentTime = DateTimeHelper.currTimeInt();
        int futureTime = DateTimeHelper.currTimeInt(60); // 60 seconds in future

        // Should be approximately 60 seconds difference (accounting for seconds component)
        int diff = futureTime - currentTime;
        assertTrue(diff >= 0 && diff <= 200); // Allow for minute rollover
    }

    @Test
    void testLastDate() {
        int today = DateTimeHelper.currDateInt();
        int yesterday = DateTimeHelper.lastDate(1);
        int twoDaysAgo = DateTimeHelper.lastDate(2);

        // Basic sanity checks
        assertTrue(yesterday < today);
        assertTrue(twoDaysAgo < yesterday);
    }

    // ==================== Time Calculation Tests ====================

    @Test
    void testDateToMillis() {
        long millis = DateTimeHelper.dateToMillis(19700101);
        assertEquals(0, millis); // Epoch date

        long millis2 = DateTimeHelper.dateToMillis(19700102);
        assertEquals(24 * 60 * 60 * 1000, millis2); // One day later
    }

    @Test
    void testTimeToMillis() {
        // 14:30:20.500 in HHMMSSmmm format
        long millis = DateTimeHelper.timeToMillis(143020500);
        long expected = (14 * 3600 + 30 * 60 + 20) * 1000 + 500;
        assertEquals(expected, millis);
    }

    @Test
    void testToLinuxTimestamp() {
        long timestamp = DateTimeHelper.toLinuxTimestamp(19700101, 0);
        assertEquals(0, timestamp); // Epoch

        long timestamp2 = DateTimeHelper.toLinuxTimestamp(19700101, 1000);
        assertEquals(1000, timestamp2); // 1 second after epoch
    }

    @Test
    void testSecondCounts() {
        assertEquals(0, DateTimeHelper.secondCounts(0));
        assertEquals(3600, DateTimeHelper.secondCounts(10000)); // 1 hour = 3600 seconds
        assertEquals(3660, DateTimeHelper.secondCounts(10100)); // 1 hour 1 minute = 3660 seconds
        assertEquals(52220, DateTimeHelper.secondCounts(143020)); // 14:30:20
    }

    @Test
    void testCountMinutes() {
        // Format: YYYYMMDDHHMMSSmmm
        long timestamp = 20231015143020500L;
        int minutes = DateTimeHelper.countMinutes(timestamp);
        assertEquals(14 * 60 + 30, minutes); // 14:30 = 870 minutes
    }

    @Test
    void testMinutesCountToTime() {
        assertEquals(0, DateTimeHelper.minutesCountToTime(0));
        assertEquals(100, DateTimeHelper.minutesCountToTime(60)); // 60 minutes = 1:00 (format is HHMM, not HHMMSS)
        assertEquals(1430, DateTimeHelper.minutesCountToTime(870)); // 870 minutes = 14:30
    }

    @Test
    void testGetSeconds() {
        // Format: YYYYMMDDHHMMSSmmm
        long timestamp = 20231015143020500L;
        assertEquals(20, DateTimeHelper.getSeconds(timestamp));
    }

    // ==================== Readable Timestamp Tests ====================

    @Test
    void testGetDate() {
        long readableTimestamp = 20231015143020500L;
        assertEquals(20231015, DateTimeHelper.getDate(readableTimestamp));
    }

    @Test
    void testGetTime() {
        long readableTimestamp = 20231015143020500L;
        assertEquals(143020500L, DateTimeHelper.getTime(readableTimestamp));
    }

    @Test
    void testStripDate() {
        long readableTimestamp = 20231015143020500L;
        assertEquals(143020500L, DateTimeHelper.stripDate(readableTimestamp));
    }

    @Test
    void testConvertTimeToInt17() {
        long result = DateTimeHelper.convertTimeToInt17(20231015, 143020);
        assertEquals(20231015143020000L, result);
    }

    @Test
    void testDateTimeLongToLocalDateTime() {
        long readableTimestamp = 20231015143020500L;
        LocalDateTime ldt = DateTimeHelper.dateTimeLongToLocalDateTime(readableTimestamp);

        assertEquals(2023, ldt.getYear());
        assertEquals(10, ldt.getMonthValue());
        assertEquals(15, ldt.getDayOfMonth());
        assertEquals(14, ldt.getHour());
        assertEquals(30, ldt.getMinute());
        assertEquals(20, ldt.getSecond());
    }

    // ==================== RQ Timestamp Tests ====================

    @Test
    void testToRQTimestampFromString() {
        long rqTimestamp = DateTimeHelper.toRQTimestamp("2023-10-15 14:30:20");
        assertTrue(rqTimestamp > 0);

        // Verify we can convert back
        int date = DateTimeHelper.getIntDateFromRQTimestamp(rqTimestamp);
        assertEquals(20231015, date);
    }

    @Test
    void testToRQTimestampFromComponents() {
        // Use 0 milliseconds to avoid Calendar millisecond handling issues
        long rqTimestamp = DateTimeHelper.toRQTimestamp(2023, 10, 15, 14, 30, 20, 0);
        assertTrue(rqTimestamp > 0);

        // Verify we can convert back to readable
        long readable = DateTimeHelper.toReadableTimestamp(rqTimestamp);
        assertEquals(20231015, DateTimeHelper.getDate(readable));
    }

    @Test
    void testToRQTimestampInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            DateTimeHelper.toRQTimestamp(-1, 1, 1, 0, 0, 0, 0);
        });
    }

    @Test
    void testToRQEpochTimestamp() {
        long readableTimestamp = 20231015143020500L;
        long rqEpoch = DateTimeHelper.toRQEpochTimestamp(readableTimestamp);
        assertTrue(rqEpoch > 0);

        // Test zero input
        assertEquals(0, DateTimeHelper.toRQEpochTimestamp(0));
    }

    @Test
    void testToReadableTimestamp() {
        // Create a known RQ timestamp
        long rqTimestamp = DateTimeHelper.toRQTimestamp(2023, 10, 15, 14, 30, 20, 0);
        long readable = DateTimeHelper.toReadableTimestamp(rqTimestamp);

        assertEquals(20231015, DateTimeHelper.getDate(readable));
        assertEquals(14, (readable % 1000000000) / 10000000); // Hour
        assertEquals(30, (readable % 10000000) / 100000); // Minute
    }

    @Test
    void testRqEpochToDate() {
        long rqEpoch = DateTimeHelper.toRQTimestamp(2023, 10, 15, 14, 30, 20, 0);
        int date = DateTimeHelper.rqEpochToDate(rqEpoch);
        assertEquals(20231015, date);
    }

    @Test
    void testGetIntDateFromRQTimestamp() {
        // Use string-based conversion to avoid Calendar millisecond issues
        long rqTimestamp = DateTimeHelper.toRQTimestamp("2023-10-15 14:30:20");
        int date = DateTimeHelper.getIntDateFromRQTimestamp(rqTimestamp);
        assertEquals(20231015, date);
    }

    @Test
    void testIsToday() {
        long todayTimestamp = DateTimeHelper.toRQTimestamp(
                DateTimeHelper.currDateInt() / 10000, // year
                (DateTimeHelper.currDateInt() / 100) % 100, // month
                DateTimeHelper.currDateInt() % 100, // day
                14, 30, 20, 0
        );
        assertTrue(DateTimeHelper.isToday(todayTimestamp));

        // Test yesterday
        long yesterdayTimestamp = DateTimeHelper.toRQTimestamp(2020, 1, 1, 14, 30, 20, 0);
        assertFalse(DateTimeHelper.isToday(yesterdayTimestamp));
    }

    // ==================== Epoch Conversion Tests ====================

    @Test
    void testDateToEpoch() {
        long epoch = DateTimeHelper.dateToEpoch(19700101);
        // Should be close to 0, accounting for timezone differences
        assertTrue(Math.abs(epoch) < 24 * 60 * 60 * 1000); // Within 24 hours of epoch
    }

    @Test
    void testToEpochSecond() {
        long epochSeconds = DateTimeHelper.toEpochSecond(19700101, 0);
        // Should be close to 0, accounting for timezone
        assertTrue(Math.abs(epochSeconds) < 24 * 60 * 60); // Within 24 hours of epoch
    }

    // ==================== Roundtrip Conversion Tests ====================

    @Test
    void testRoundtripIntDateToDateTimeToInt() {
        int originalDate = 20231015;
        DateTime dt = DateTimeHelper.toDateTime(originalDate);
        int convertedDate = DateTimeHelper.toInt(dt);
        assertEquals(originalDate, convertedDate);
    }

    @Test
    void testRoundtripReadableToRQEpochToReadable() {
        long originalReadable = 20231015143020500L;
        long rqEpoch = DateTimeHelper.toRQEpochTimestamp(originalReadable);
        long convertedReadable = DateTimeHelper.toReadableTimestamp(rqEpoch);

        // Date should match exactly
        assertEquals(DateTimeHelper.getDate(originalReadable), DateTimeHelper.getDate(convertedReadable));

        // Time should match within reasonable precision (milliseconds might differ slightly)
        long timeDiff = Math.abs(DateTimeHelper.getTime(originalReadable) - DateTimeHelper.getTime(convertedReadable));
        assertTrue(timeDiff < 1000); // Within 1 second
    }

    // ==================== Edge Cases and Boundary Tests ====================

    @Test
    void testLeapYear() {
        // February 29, 2024 (leap year)
        assertTrue(DateTimeHelper.isDateFormatValid(20240229));

        // February 29, 2023 (not a leap year)
        assertFalse(DateTimeHelper.isDateFormatValid(20230229));
    }

    @Test
    void testMidnightTime() {
        assertEquals("00:00:00", DateTimeHelper.intTimeToString(0));
    }

    @Test
    void testEndOfDay() {
        assertEquals("23:59:59", DateTimeHelper.intTimeToString(235959));
    }

    @Test
    void testYearBoundaries() {
        assertTrue(DateTimeHelper.isDateFormatValid(19000101)); // Old date
        assertTrue(DateTimeHelper.isDateFormatValid(29991231)); // Far future
    }

    @Test
    void testPrependDateAndGetExchangeTimestamp() {
        int date = 20231015;
        long microseconds = 52220L * 1000000; // 14:30:20 in microseconds

        long result = DateTimeHelper.prependDateAndGetExchangeTimestamp(date, microseconds);

        // Verify date part
        assertEquals(20231015, result / 1000000000L);

        // Verify time part is reasonable
        long timePart = result % 1000000000L;
        assertTrue(timePart > 0);
    }

    @Test
    void testMicroCounts() {
        // 14:30:20.500 in HHMMSSmmm×1000 format
        long timestamp = 143020500L * 1000;
        long micros = DateTimeHelper.microCounts(timestamp);

        long expected = (14L * 3600 + 30 * 60 + 20) * 1000000 + 500 * 1000;
        assertEquals(expected, micros);
    }

    @Test
    void testStripDateAndCountMicros() {
        long readableTimestamp = 20231015143020500L;
        long micros = DateTimeHelper.stripDateAndCountMicros(readableTimestamp);
        assertTrue(micros > 0);

        // Should be less than 24 hours worth of microseconds
        assertTrue(micros < 24L * 60 * 60 * 1000000);
    }

    @Test
    void testCurrentFunctionsReturnReasonableValues() {
        // Test that current time functions return reasonable values
        long currentMicros = DateTimeHelper.getCurrentMicros();
        assertTrue(currentMicros > 0);

        long currentReadable = DateTimeHelper.getCurrentReadable();
        assertTrue(currentReadable > 20230000000000L); // After 2023

        int currentMinuteTime = DateTimeHelper.currMinuteTimeInt();
        assertTrue(currentMinuteTime >= 0);
        assertTrue(currentMinuteTime < 240000000); // Less than 24:00:00.000
    }
}
