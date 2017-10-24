package io.arusland.bots.utils;

import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

        assertEquals("several seconds", ts);
    }

    @Test
    public void testFriendlyTimespan15Seconds() throws ParseException {
        String ts = friendlyTimespan(new Date(DF.parse("22:09 24.10.2017").getTime() + 1000*15));

        assertEquals("15 seconds", ts);
    }

    @Test
    public void testFriendlyTimespan1Min() throws ParseException {
        String ts = friendlyTimespan("22:10 24.10.2017");

        assertEquals("1 minutes", ts);
    }

    @Test
    public void testFriendlyTimespan1Hours() throws ParseException {
        String ts = friendlyTimespan("23:09 24.10.2017");

        assertEquals("1 hours", ts);
    }

    @Test
    public void testFriendlyTimespan1Hours1Min() throws ParseException {
        String ts = friendlyTimespan("23:10 24.10.2017");

        assertEquals("1 hours 1 minutes", ts);
    }

    @Test
    public void testFriendlyTimespan1Hours1Day() throws ParseException {
        String ts = friendlyTimespan("22:09 25.10.2017");

        assertEquals("1 days", ts);
    }

    @Test
    public void testFriendlyTimespan1Month() throws ParseException {
        String ts = friendlyTimespan("22:09 25.11.2017");

        assertEquals("1 months (32 days)", ts);
    }

    @Test
    public void testFriendlyTimespan2Months() throws ParseException {
        String ts = friendlyTimespan("22:09 26.12.2017");

        assertEquals("2 months (63 days)", ts);
    }

    @Test
    public void testFriendlyTimespan1Years() throws ParseException {
        String ts = friendlyTimespan("22:09 25.11.2018");

        assertEquals("1 years 1 months", ts);
    }

    @Test
    public void testFriendlyTimespan1Years17Days() throws ParseException {
        String ts = friendlyTimespan("22:09 10.11.2018");

        assertEquals("1 years 17 days", ts);
    }

    @Test
    public void testFriendlyTimespan11Years17Days() throws ParseException {
        String ts = friendlyTimespan("22:09 10.11.2028");

        assertEquals("11 years 2 months", ts);
    }

    @Test
    public void testFriendlyTimespanBug3() throws ParseException {
        String ts = friendlyTimespan("13:00 12.11.2017");

        assertEquals("18 days 14 hours", ts);
    }

    private String friendlyTimespan(String s) throws ParseException {
        return TimeUtils.friendlyTimespan(DF.parse(s), frozenTime);
    }

    private String friendlyTimespan(Date date) throws ParseException {
        return TimeUtils.friendlyTimespan(date, frozenTime);
    }
}
