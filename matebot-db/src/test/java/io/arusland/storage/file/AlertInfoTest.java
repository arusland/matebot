package io.arusland.storage.file;

import io.arusland.storage.TestUtils;
import io.arusland.storage.TimeZoneClient;
import io.arusland.storage.TimeZoneClientStandard;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Created by ruslan on 10.12.2016.
 */
public class AlertInfoTest {
    private final static TimeZoneClient timeZoneClient = new TimeZoneClientStandard();

    @Test
    public void testTimeZoneClient() {
        TimeZoneClient timeZoneTashkent = TimeZoneClientStandard.create(TimeZone.getTimeZone("GMT+5:00"));
        Calendar calNow = Calendar.getInstance();
        calNow.set(Calendar.MILLISECOND, 0); // clean milliseconds
        Date localTime = calNow.getTime();
        Date clientTime = timeZoneTashkent.toClient(localTime);
        System.out.println("Local timezone: " + TimeZone.getDefault().getDisplayName());
        System.out.println("Client timezone: " + timeZoneTashkent.getTimeZone().getDisplayName());
        System.out.println("Local time: " + localTime);
        System.out.println("Client time: " + clientTime);
        Date localTime2 = timeZoneTashkent.fromClient(clientTime);
        assertFalse(localTime.equals(clientTime));
        assertEquals(localTime, localTime2);

        if (timeZoneTashkent.getTimeZone().getRawOffset() > TimeZone.getDefault().getRawOffset()) {
            assertTrue(clientTime.getTime() > localTime.getTime());
        } else {
            assertTrue(clientTime.getTime() < localTime.getTime());
        }
    }

    @Test
    public void testFullWithMessage() {
        AlertInfo info = parseInfo("7:43 05:10:2012 Malik birthday ");

        assertTrue(info.valid);
        assertEquals(7, info.hour);
        assertEquals(43, info.minute);
        assertEquals(5, (int) info.day);
        assertEquals(10, (int) info.month);
        assertEquals(2012, (int) info.year);
        assertEquals(0, info.weekDays);
        assertEquals("Malik birthday", info.message);
        assertEquals("7:43 05:10:2012 Malik birthday", info.content);
    }

    @Test
    public void testFullWithoutMessage() {
        AlertInfo info = parseInfo("07:03 5:12:2017");

        assertTrue(info.valid);
        assertEquals(7, info.hour);
        assertEquals(3, info.minute);
        assertEquals(5, (int) info.day);
        assertEquals(12, (int) info.month);
        assertEquals(2017, (int) info.year);
        assertEquals(0, info.weekDays);
        assertEquals("", info.message);
        assertEquals("07:03 5:12:2017", info.content);
    }

    @Test
    public void testFullWithEmptyMessage() {
        AlertInfo info = parseInfo("07:03 5:12:2017 ");

        assertTrue(info.valid);
        assertEquals(7, info.hour);
        assertEquals(3, info.minute);
        assertEquals(5, (int) info.day);
        assertEquals(12, (int) info.month);
        assertEquals(2017, (int) info.year);
        assertEquals(0, info.weekDays);
        assertEquals("", info.message);
        assertEquals("07:03 5:12:2017", info.content);
    }

    @Test
    public void testFullWithEmptyMessageStartsWithSpace() {
        AlertInfo info = parseInfo(" 07:03 5:12:2017");

        assertTrue(info.valid);
        assertEquals(7, info.hour);
        assertEquals(3, info.minute);
        assertEquals(5, (int) info.day);
        assertEquals(12, (int) info.month);
        assertEquals(2017, (int) info.year);
        assertEquals(0, info.weekDays);
        assertEquals("", info.message);
        assertEquals("07:03 5:12:2017", info.content);
    }

    @Test
    public void testFullWithoutYearWithMessage() {
        AlertInfo info = parseInfo("07:3 23:2 My message");

        assertTrue(info.valid);
        assertEquals(7, info.hour);
        assertEquals(3, info.minute);
        assertEquals(23, (int) info.day);
        assertEquals(2, (int) info.month);
        assertNull(info.year);
        assertEquals(0, info.weekDays);
        assertEquals("My message", info.message);
        assertEquals("07:3 23:2 My message", info.content);
    }

    @Test
    public void testFullWithoutYearWithEmptyMessage() {
        AlertInfo info = parseInfo("07:3 23:2 ");

        assertTrue(info.valid);
        assertEquals(7, info.hour);
        assertEquals(3, info.minute);
        assertEquals(23, (int) info.day);
        assertEquals(2, (int) info.month);
        assertNull(info.year);
        assertEquals(0, info.weekDays);
        assertEquals("", info.message);
        assertEquals("07:3 23:2", info.content);
    }

    @Test
    public void testFullWithoutYearWithoutMessage() {
        AlertInfo info = parseInfo("07:3 23:2");

        assertTrue(info.valid);
        assertEquals(7, info.hour);
        assertEquals(3, info.minute);
        assertEquals(23, (int) info.day);
        assertEquals(2, (int) info.month);
        assertNull(info.year);
        assertEquals(0, info.weekDays);
        assertEquals("", info.message);
        assertEquals("07:3 23:2", info.content);
    }

    @Test
    public void testFullWithoutYearWithMessage2() {
        AlertInfo info = parseInfo("07:3 23:2: My message");

        assertTrue(info.valid);
        assertEquals(7, info.hour);
        assertEquals(3, info.minute);
        assertEquals(23, (int) info.day);
        assertEquals(2, (int) info.month);
        assertNull(info.year);
        assertEquals(0, info.weekDays);
        assertEquals("My message", info.message);
        assertEquals("07:3 23:2: My message", info.content);
    }

    @Test
    public void testFullWithoutYearWithEmptyMessage2() {
        AlertInfo info = parseInfo("07:3 23:2: ");

        assertTrue(info.valid);
        assertEquals(7, info.hour);
        assertEquals(3, info.minute);
        assertEquals(23, (int) info.day);
        assertEquals(2, (int) info.month);
        assertNull(info.year);
        assertEquals(0, info.weekDays);
        assertEquals("", info.message);
        assertEquals("07:3 23:2:", info.content);
    }

    @Test
    public void testFullWithoutYearWithoutMessage2() {
        AlertInfo info = parseInfo("07:3 23:2:");

        assertTrue(info.valid);
        assertEquals(7, info.hour);
        assertEquals(3, info.minute);
        assertEquals(23, (int) info.day);
        assertEquals(2, (int) info.month);
        assertNull(info.year);
        assertEquals(0, info.weekDays);
        assertEquals("", info.message);
        assertEquals("07:3 23:2:", info.content);
    }

    @Test
    public void testFullWithoutYearAndMonthWithMessage() {
        AlertInfo info = parseInfo("14:33 7: Hello !");

        assertTrue(info.valid);
        assertEquals(14, info.hour);
        assertEquals(33, info.minute);
        assertEquals(7, (int) info.day);
        assertNull(info.month);
        assertNull(info.year);
        assertEquals(0, info.weekDays);
        assertEquals("Hello !", info.message);
        assertEquals("14:33 7: Hello !", info.content);
    }

    @Test
    public void testFullWithoutYearAndMonthWithEmptyMessage() {
        AlertInfo info = parseInfo("14:33 7: ");

        assertTrue(info.valid);
        assertEquals(14, info.hour);
        assertEquals(33, info.minute);
        assertEquals(7, (int) info.day);
        assertNull(info.month);
        assertNull(info.year);
        assertEquals(0, info.weekDays);
        assertEquals("", info.message);
        assertEquals("14:33 7:", info.content);
    }

    @Test
    public void testFullWithoutYearAndMonthWithoutMessage() {
        AlertInfo info = parseInfo("14:33 7:");

        assertTrue(info.valid);
        assertEquals(14, info.hour);
        assertEquals(33, info.minute);
        assertEquals(7, (int) info.day);
        assertNull(info.month);
        assertNull(info.year);
        assertEquals(0, info.weekDays);
        assertEquals("", info.message);
        assertEquals("14:33 7:", info.content);
    }

    @Test
    public void testFullWithoutYearAndMonthWithMessageLikeDay() {
        AlertInfo info = parseInfo("14:33 7d");
        Calendar cal = calcNextDayAfterTime(info);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DATE);

        assertTrue(info.valid);
        assertEquals(14, info.hour);
        assertEquals(33, info.minute);
        assertEquals(day, (int) info.day);
        assertEquals(month, (int) info.month);
        assertEquals(year, (int) info.year);
        assertEquals(0, info.weekDays);
        assertEquals("7d", info.message);
        assertEquals(String.format("14:33 %d:%d:%d 7d", day, month, year), info.content);
    }

    @Test
    public void testShortWithMessage() {
        AlertInfo info = parseInfo("23:59 Alert message!");
        Calendar cal = calcNextDayAfterTime(info);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DATE);

        assertTrue(info.valid);
        assertEquals(23, info.hour);
        assertEquals(59, info.minute);
        assertEquals(day, (int) info.day);
        assertEquals(month, (int) info.month);
        assertEquals(year, (int) info.year);
        assertEquals(0, info.weekDays);
        assertEquals("Alert message!", info.message);
        assertEquals(String.format("23:59 %d:%d:%d Alert message!", day, month, year), info.content);
    }

    @Test
    public void testShortWithEmptyMessage() {
        AlertInfo info = parseInfo("22:1 ");
        Calendar cal = calcNextDayAfterTime(info);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DATE);

        assertTrue(info.valid);
        assertEquals(22, info.hour);
        assertEquals(1, info.minute);
        assertEquals(day, (int) info.day);
        assertEquals(month, (int) info.month);
        assertEquals(year, (int) info.year);
        assertEquals(0, info.weekDays);
        assertEquals("", info.message);
        assertEquals(String.format("22:1 %d:%d:%d", day, month, year), info.content);
    }

    @Test
    public void testShortWithoutMessage() {
        AlertInfo info = parseInfo("12:21");
        Calendar cal = calcNextDayAfterTime(info);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DATE);

        assertTrue(info.valid);
        assertEquals(12, info.hour);
        assertEquals(21, info.minute);
        assertEquals(day, (int) info.day);
        assertEquals(month, (int) info.month);
        assertEquals(year, (int) info.year);
        assertEquals(0, info.weekDays);
        assertEquals("", info.message);
        assertEquals(String.format("12:21 %d:%d:%d", day, month, year), info.content);
    }

    @Test
    public void testShortWithoutMessageStartsWithSpace() {
        AlertInfo info = parseInfo(" 12:21");
        Calendar cal = calcNextDayAfterTime(info);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DATE);

        assertTrue(info.valid);
        assertEquals(12, info.hour);
        assertEquals(21, info.minute);
        assertEquals(day, (int) info.day);
        assertEquals(month, (int) info.month);
        assertEquals(year, (int) info.year);
        assertEquals(0, info.weekDays);
        assertEquals("", info.message);
        assertEquals(String.format("12:21 %d:%d:%d", day, month, year), info.content);
    }

    @Test
    public void testShortWeekDaysWithMessage() {
        AlertInfo info = parseInfo("23:59 1-2,7 Alert message!");

        assertTrue(info.valid);
        assertEquals(23, info.hour);
        assertEquals(59, info.minute);
        assertNull(info.day);
        assertNull(info.month);
        assertNull(info.year);
        assertEquals(AlertInfo.DAY_MONDAY | AlertInfo.DAY_TUESDAY | AlertInfo.DAY_SUNDAY, info.weekDays);
        assertEquals("Alert message!", info.message);
        assertEquals("23:59 1-2,7 Alert message!", info.content);
    }

    @Test
    public void testShortWeekDaysWithEmptyMessage() {
        AlertInfo info = parseInfo("23:59 5 ");

        assertTrue(info.valid);
        assertEquals(23, info.hour);
        assertEquals(59, info.minute);
        assertNull(info.day);
        assertNull(info.month);
        assertNull(info.year);
        assertEquals(AlertInfo.DAY_FRIDAY, info.weekDays);
        assertEquals("", info.message);
        assertEquals("23:59 5", info.content);
    }

    @Test
    public void testShortWeekDaysWithoutMessage() {
        AlertInfo info = parseInfo("23:59 5-7");

        assertTrue(info.valid);
        assertEquals(23, info.hour);
        assertEquals(59, info.minute);
        assertNull(info.day);
        assertNull(info.month);
        assertNull(info.year);
        assertEquals(AlertInfo.DAY_FRIDAY | AlertInfo.DAY_SATURDAY | AlertInfo.DAY_SUNDAY, info.weekDays);
        assertEquals("", info.message);
        assertEquals("23:59 5-7", info.content);
    }

    @Test
    public void testShortWrongWeekDaysWithMessage() {
        AlertInfo info = parseInfo("23:59 5-8 strange message");
        Calendar cal = calcNextDayAfterTime(info);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DATE);

        assertTrue(info.valid);
        assertEquals(23, info.hour);
        assertEquals(59, info.minute);
        assertEquals(day, (int) info.day);
        assertEquals(month, (int) info.month);
        assertEquals(year, (int) info.year);
        assertEquals(0, info.weekDays);
        assertEquals("5-8 strange message", info.message);
        assertEquals(String.format("23:59 %d:%d:%d 5-8 strange message", day, month, year), info.content);
    }

    @Test
    public void testShortWithMessageAndPeriodInMinutes() {
        AlertInfo info = parseInfo("23:59/1 Alert message!");
        Calendar cal = calcNextDayAfterTime(info);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DATE);

        assertTrue(info.valid);
        assertEquals(23, info.hour);
        assertEquals(59, info.minute);
        assertEquals(day, (int) info.day);
        assertEquals(month, (int) info.month);
        assertEquals(year, (int) info.year);
        assertEquals(0, info.weekDays);
        assertEquals(1, (int) info.period);
        assertEquals(ChronoUnit.MINUTES, info.periodType);
        assertEquals("Alert message!", info.message);
        assertEquals(String.format("23:59/1 %d:%d:%d Alert message!", day, month, year), info.content);
    }

    @Test
    public void testShortWithMessageAndPeriodInHours() {
        AlertInfo info = parseInfo("23/3:59 Alert message!");
        Calendar cal = calcNextDayAfterTime(info);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DATE);

        assertTrue(info.valid);
        assertEquals(23, info.hour);
        assertEquals(59, info.minute);
        assertEquals(day, (int) info.day);
        assertEquals(month, (int) info.month);
        assertEquals(year, (int) info.year);
        assertEquals(0, info.weekDays);
        assertEquals(3, (int) info.period);
        assertEquals(ChronoUnit.HOURS, info.periodType);
        assertEquals("Alert message!", info.message);
        assertEquals(String.format("23/3:59 %d:%d:%d Alert message!", day, month, year), info.content);
    }

    @Test
    public void testShortWeekDaysWithoutMessageAndPeriodInMinutes() {
        AlertInfo info = parseInfo("23:59/4 5-7");

        assertTrue(info.valid);
        assertEquals(23, info.hour);
        assertEquals(59, info.minute);
        assertNull(info.day);
        assertNull(info.month);
        assertNull(info.year);
        assertEquals(4, (int) info.period);
        assertEquals(ChronoUnit.MINUTES, info.periodType);
        assertEquals(AlertInfo.DAY_FRIDAY | AlertInfo.DAY_SATURDAY | AlertInfo.DAY_SUNDAY, info.weekDays);
        assertEquals("", info.message);
        assertEquals("23:59/4 5-7", info.content);
    }

    @Test
    public void testFullWithoutYearAndMonthWithPeriod() {
        AlertInfo info = parseInfo("14:33 7/2:");

        assertTrue(info.valid);
        assertEquals(14, info.hour);
        assertEquals(33, info.minute);
        assertEquals(7, (int) info.day);
        assertNull(info.month);
        assertNull(info.year);
        assertEquals(0, info.weekDays);
        assertEquals("", info.message);
        assertEquals(2, (int) info.period);
        assertEquals(ChronoUnit.DAYS, info.periodType);
        assertEquals("14:33 7/2:", info.content);
    }

    @Test
    public void testFullWithoutYearWithMessageWithPeriod() {
        AlertInfo info = parseInfo("07:3 23:2/1  Hi folks!");

        assertTrue(info.valid);
        assertEquals(7, info.hour);
        assertEquals(3, info.minute);
        assertEquals(23, (int) info.day);
        assertEquals(2, (int) info.month);
        assertNull(info.year);
        assertEquals(0, info.weekDays);
        assertEquals("Hi folks!", info.message);
        assertEquals(1, (int) info.period);
        assertEquals(ChronoUnit.MONTHS, info.periodType);
        assertEquals("07:3 23:2/1  Hi folks!", info.content);
    }

    @Test
    public void testFullWithoutYearWithMessage2WithPeriod() {
        AlertInfo info = parseInfo("07:3 23:2/3:  Hi folks!");

        assertTrue(info.valid);
        assertEquals(7, info.hour);
        assertEquals(3, info.minute);
        assertEquals(23, (int) info.day);
        assertEquals(2, (int) info.month);
        assertNull(info.year);
        assertEquals(0, info.weekDays);
        assertEquals("Hi folks!", info.message);
        assertEquals(3, (int) info.period);
        assertEquals(ChronoUnit.MONTHS, info.periodType);
        assertEquals("07:3 23:2/3:  Hi folks!", info.content);
    }

    @Test
    public void testFullWithMessageWithPeriod() {
        AlertInfo info = parseInfo("7:43 05:10:2012/5 Malik birthday ");

        assertTrue(info.valid);
        assertEquals(7, info.hour);
        assertEquals(43, info.minute);
        assertEquals(5, (int) info.day);
        assertEquals(10, (int) info.month);
        assertEquals(2012, (int) info.year);
        assertEquals(0, info.weekDays);
        assertEquals("Malik birthday", info.message);
        assertEquals(5, (int) info.period);
        assertEquals(ChronoUnit.YEARS, info.periodType);
        assertEquals("7:43 05:10:2012/5 Malik birthday", info.content);
    }

    @Test
    public void testFullWithMessageWithMonthPeriod() {
        AlertInfo info = parseInfo("23:00 17:08/1:2018 please, fix this bug!");

        assertTrue(info.valid);
        assertEquals(23, info.hour);
        assertEquals(0, info.minute);
        assertEquals(17, (int) info.day);
        assertEquals(8, (int) info.month);
        assertEquals(2018, (int)info.year);
        assertEquals(0, info.weekDays);
        assertEquals("please, fix this bug!", info.message);
        assertEquals(1, (int) info.period);
        assertEquals(ChronoUnit.MONTHS, info.periodType);
        assertEquals("23:00 17:08/1:2018 please, fix this bug!", info.content);
    }

    // invalid dates tests

    @Test
    public void testShortWeekDaysWithoutMessageAndPeriodInvalid() {
        AlertInfo info = parseInfo("23/1:59/4 5-7");

        assertFalse(info.valid);
        assertEquals("Period can be applied only once", info.validMessage);
    }

    @Test
    public void testFullWithDayWithoutMessageAndPeriodInvalid() {
        AlertInfo info = parseInfo("23/1:59/4 13:");

        assertFalse(info.valid);
        assertEquals("Period can be applied only once", info.validMessage);
    }

    @Test
    public void testFullWithMonthWithoutMessageAndPeriodInvalid() {
        AlertInfo info = parseInfo("23/1:59/4 13:04/2");

        assertFalse(info.valid);
        assertEquals("Period can be applied only once", info.validMessage);
    }

    @Test
    public void testFullWithoutMessageAndPeriodInvalid() {
        AlertInfo info = parseInfo("23:59 13:04/2:2019/1");

        assertFalse(info.valid);
        assertEquals("Period can be applied only once", info.validMessage);
    }

    @Test
    public void testFullWithMessageInvalid() {
        AlertInfo info = parseInfo("7:43 05:13:2012 Malik birthday ");

        assertFalse(info.valid);
    }

    @Test
    public void testFullWith29FebInvalid() {
        AlertInfo info = parseInfo("7:43 29:2:2001 ");

        assertFalse(info.valid);
    }

    @Test
    public void testFullWithZeroDayInvalid() {
        AlertInfo info = parseInfo("7:43 0:02:2001");

        assertFalse(info.valid);
    }

    @Test
    public void testFullWithZeroMonthNovInvalid() {
        AlertInfo info = parseInfo("7:43 1:0:2012  ");

        assertFalse(info.valid);
    }

    @Test
    public void testFullWith31NovInvalid() {
        AlertInfo info = parseInfo("7:43 31:11:2000");

        assertFalse(info.valid);
    }

    @Test
    public void testWithTimeInvalid() {
        AlertInfo info = parseInfo("24:00 ");

        assertFalse(info.valid);
    }

    @Test
    public void testWithTimeMinuteInvalid() {
        AlertInfo info = parseInfo("13:60 foo bar");

        assertFalse(info.valid);
    }

    @Test
    public void testFormatInvalid() {
        AlertInfo info = parseInfo("13:");

        assertNull(info);
    }

    @Test
    public void testFormatInvalid2() {
        AlertInfo info = parseInfo("13: ");

        assertNull(info);
    }

    @Test
    public void testFormatInvalid3() {
        AlertInfo info = parseInfo("2");

        assertNull(info);
    }

    @Test
    public void testFormatInvalid4() {
        AlertInfo info = parseInfo("05:5D");

        assertNull(info);
    }

    @Test
    public void testFormatInvalid5() {
        AlertInfo info = parseInfo("");

        assertNull(info);
    }

    @Test
    public void testFormatInvalid6() {
        AlertInfo info = parseInfo(null);

        assertNull(info);
    }

    private static Calendar calcNextDayAfterTime(AlertInfo info) {
        return TestUtils.calcNextDayAfterTime(info.hour, info.minute, timeZoneClient);
    }

    private static AlertInfo parseInfo(String input) {
        return AlertInfo.parse(input, timeZoneClient);
    }
}
