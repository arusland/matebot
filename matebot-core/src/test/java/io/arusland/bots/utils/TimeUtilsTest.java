package io.arusland.bots.utils;

import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * @author Ruslan Absalyamov
 * @since 2017-10-24
 */
public class TimeUtilsTest {
    private static final SimpleDateFormat DF = new SimpleDateFormat("HH:mm dd.MM.yyyy");
    private long frozenTime;

    @Before
    public void init() throws ParseException {
        frozenTime = DF.parse("22:09 24.10.2017").getTime();
    }

    @Test
    public void testFriendlyTimespanSeveralSeconds() throws ParseException {
        String ts = friendlyTimespan("22:09 24.10.2017");
        String ss = friendlyTimespanShort("22:09 24.10.2017");

        assertEquals("few moments", ts);
        assertEquals("few moments", ss);
    }

    @Test
    public void testFriendlyTimespan15Seconds() throws ParseException {
        String ts = friendlyTimespan(new Date(DF.parse("22:09 24.10.2017").getTime() + 1000 * 15));
        String ss = friendlyTimespanShort(new Date(DF.parse("22:09 24.10.2017").getTime() + 1000 * 15));

        assertEquals("15 seconds", ts);
        assertEquals("15 s.", ss);
    }

    @Test
    public void testFriendlyTimespan1Min() throws ParseException {
        String ts = friendlyTimespan("22:10 24.10.2017");
        String ss = friendlyTimespanShort("22:10 24.10.2017");

        assertEquals("1 minute", ts);
        assertEquals("1 m.", ss);
    }

    @Test
    public void testFriendlyTimespan1Hours() throws ParseException {
        String ts = friendlyTimespan("23:09 24.10.2017");
        String ss = friendlyTimespanShort("23:09 24.10.2017");

        assertEquals("1 hour", ts);
        assertEquals("1 h.", ss);
    }

    @Test
    public void testFriendlyTimespan1Hours1Min() throws ParseException {
        String ts = friendlyTimespan("23:10 24.10.2017");
        String ss = friendlyTimespanShort("23:10 24.10.2017");

        assertEquals("1 hour 1 minute", ts);
        assertEquals("1 h. 1 m.", ss);
    }

    @Test
    public void testFriendlyTimespan1Hours1Day() throws ParseException {
        String ts = friendlyTimespan("22:09 25.10.2017");
        String ss = friendlyTimespanShort("22:09 25.10.2017");

        assertEquals("1 day", ts);
        assertEquals("1 d.", ss);
    }

    @Test
    public void testFriendlyTimespan1Month() throws ParseException {
        String ts = friendlyTimespan("22:09 25.11.2017");
        String ss = friendlyTimespanShort("22:09 25.11.2017");

        assertEquals("1 month (32 days)", ts);
        assertEquals("1 mon. (32 d.)", ss);
    }

    @Test
    public void testFriendlyTimespan2Months() throws ParseException {
        String ts = friendlyTimespan("22:09 26.12.2017");
        String ss = friendlyTimespanShort("22:09 26.12.2017");

        assertEquals("2 months (63 days)", ts);
        assertEquals("2 mon. (63 d.)", ss);
    }

    @Test
    public void testFriendlyTimespan1Years() throws ParseException {
        String ts = friendlyTimespan("22:09 25.11.2018");
        String ss = friendlyTimespanShort("22:09 25.11.2018");

        assertEquals("1 year 1 month", ts);
        assertEquals("1 y. 1 mon.", ss);
    }

    @Test
    public void testFriendlyTimespan1Years17Days() throws ParseException {
        String ts = friendlyTimespan("22:09 10.11.2018");
        String ss = friendlyTimespanShort("22:09 10.11.2018");

        assertEquals("1 year 17 days", ts);
        assertEquals("1 y. 17 d.", ss);
    }

    @Test
    public void testFriendlyTimespan11Years17Days() throws ParseException {
        String ts = friendlyTimespan("22:09 10.11.2028");
        String ss = friendlyTimespanShort("22:09 10.11.2028");

        assertEquals("11 years 2 months", ts);
        assertEquals("11 y. 2 mon.", ss);
    }

    @Test
    public void testFriendlyTimespanBug3() throws ParseException {
        String ts = friendlyTimespan("13:00 12.11.2017");
        String ss = friendlyTimespanShort("13:00 12.11.2017");

        assertEquals("18 days 14 hours", ts);
        assertEquals("18 d. 14 h.", ss);
    }

    @Test
    public void testFriendlyPeriod() throws ParseException {
        assertEquals("1 hour", TimeUtils.friendlyPeriod(1, ChronoUnit.HOURS));
        assertEquals("3 hours", TimeUtils.friendlyPeriod(3, ChronoUnit.HOURS));
        assertEquals("1 minute", TimeUtils.friendlyPeriod(1, ChronoUnit.MINUTES));
        assertEquals("5 minutes", TimeUtils.friendlyPeriod(5, ChronoUnit.MINUTES));
        assertEquals("1 second", TimeUtils.friendlyPeriod(1, ChronoUnit.SECONDS));
        assertEquals("12 seconds", TimeUtils.friendlyPeriod(12, ChronoUnit.SECONDS));
        assertEquals("1 day", TimeUtils.friendlyPeriod(1, ChronoUnit.DAYS));
        assertEquals("5 days", TimeUtils.friendlyPeriod(5, ChronoUnit.DAYS));
        assertEquals("1 month", TimeUtils.friendlyPeriod(1, ChronoUnit.MONTHS));
        assertEquals("2 months", TimeUtils.friendlyPeriod(2, ChronoUnit.MONTHS));
        assertEquals("1 year", TimeUtils.friendlyPeriod(1, ChronoUnit.YEARS));
        assertEquals("7 years", TimeUtils.friendlyPeriod(7, ChronoUnit.YEARS));
    }

    private String friendlyTimespan(String s) throws ParseException {
        return TimeUtils.friendlyTimespan(DF.parse(s), frozenTime);
    }

    private String friendlyTimespan(Date date) throws ParseException {
        return TimeUtils.friendlyTimespan(date, frozenTime);
    }

    private String friendlyTimespanShort(String s) throws ParseException {
        return TimeUtils.friendlyTimespanShort(DF.parse(s), frozenTime);
    }

    private String friendlyTimespanShort(Date date) throws ParseException {
        return TimeUtils.friendlyTimespanShort(date, frozenTime);
    }
}
