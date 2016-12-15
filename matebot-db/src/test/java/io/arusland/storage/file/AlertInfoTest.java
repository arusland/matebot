package io.arusland.storage.file;

import io.arusland.storage.TestUtils;
import org.junit.Test;

import java.util.Calendar;

import static junit.framework.TestCase.*;

/**
 * Created by ruslan on 10.12.2016.
 */
public class AlertInfoTest {
    @Test
    public void testFullWithMessage() {
        AlertInfo info = AlertInfo.parse("7:43 05:10:2012 Malik birthday ");

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
        AlertInfo info = AlertInfo.parse("07:03 5:12:2017");

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
        AlertInfo info = AlertInfo.parse("07:03 5:12:2017 ");

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
        AlertInfo info = AlertInfo.parse(" 07:03 5:12:2017");

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
        AlertInfo info = AlertInfo.parse("07:3 23:2 My message");

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
        AlertInfo info = AlertInfo.parse("07:3 23:2 ");

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
        AlertInfo info = AlertInfo.parse("07:3 23:2");

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
    public void testFullWithoutYearAndMonthWithMessage() {
        AlertInfo info = AlertInfo.parse("14:33 7: Hello !");

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
        AlertInfo info = AlertInfo.parse("14:33 7: ");

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
        AlertInfo info = AlertInfo.parse("14:33 7:");

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
        AlertInfo info = AlertInfo.parse("14:33 7d");
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
        AlertInfo info = AlertInfo.parse("23:59 Alert message!");
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
        AlertInfo info = AlertInfo.parse("22:1 ");
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
        AlertInfo info = AlertInfo.parse("12:21");
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
        AlertInfo info = AlertInfo.parse(" 12:21");
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
        AlertInfo info = AlertInfo.parse("23:59 1-2,7 Alert message!");

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
        AlertInfo info = AlertInfo.parse("23:59 5 ");

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
        AlertInfo info = AlertInfo.parse("23:59 5-7");

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
        AlertInfo info = AlertInfo.parse("23:59 5-8 strange message");
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

    // invalid dates tests

    @Test
    public void testFullWithMessageInvalid() {
        AlertInfo info = AlertInfo.parse("7:43 05:13:2012 Malik birthday ");

        assertFalse(info.valid);
    }

    @Test
    public void testFullWith29FebInvalid() {
        AlertInfo info = AlertInfo.parse("7:43 29:2:2001 ");

        assertFalse(info.valid);
    }

    @Test
    public void testFullWithZeroDayInvalid() {
        AlertInfo info = AlertInfo.parse("7:43 0:02:2001");

        assertFalse(info.valid);
    }

    @Test
    public void testFullWithZeroMonthNovInvalid() {
        AlertInfo info = AlertInfo.parse("7:43 1:0:2012  ");

        assertFalse(info.valid);
    }

    @Test
    public void testFullWith31NovInvalid() {
        AlertInfo info = AlertInfo.parse("7:43 31:11:2000");

        assertFalse(info.valid);
    }

    @Test
    public void testWithTimeInvalid() {
        AlertInfo info = AlertInfo.parse("24:00 ");

        assertFalse(info.valid);
    }

    @Test
    public void testWithTimeMinuteInvalid() {
        AlertInfo info = AlertInfo.parse("13:60 foo bar");

        assertFalse(info.valid);
    }

    @Test
    public void testFormatInvalid() {
        AlertInfo info = AlertInfo.parse("13:");

        assertNull(info);
    }

    @Test
    public void testFormatInvalid2() {
        AlertInfo info = AlertInfo.parse("13: ");

        assertNull(info);
    }

    @Test
    public void testFormatInvalid3() {
        AlertInfo info = AlertInfo.parse("2");

        assertNull(info);
    }

    @Test
    public void testFormatInvalid4() {
        AlertInfo info = AlertInfo.parse("05:5D");

        assertNull(info);
    }

    @Test
    public void testFormatInvalid5() {
        AlertInfo info = AlertInfo.parse("");

        assertNull(info);
    }

    @Test
    public void testFormatInvalid6() {
        AlertInfo info = AlertInfo.parse(null);

        assertNull(info);
    }

    private static Calendar calcNextDayAfterTime(AlertInfo info) {
        return TestUtils.calcNextDayAfterTime(info.hour, info.minute);
    }
}
