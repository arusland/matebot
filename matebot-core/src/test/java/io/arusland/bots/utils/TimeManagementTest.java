package io.arusland.bots.utils;

import org.junit.Test;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by ruslan on 13.12.2016.
 */
public class TimeManagementTest {
    @Test
    public void testScheduling() throws InterruptedException {
        TimeManagement timeMgr = new TimeManagement();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 2);
        AtomicBoolean fired = new AtomicBoolean(false);
        long last = System.currentTimeMillis();

        timeMgr.enqueue(cal.getTime(), () -> {
            assertTrue((System.currentTimeMillis() - last) > 1000);
            fired.set(true);
        });

        assertEquals(1, timeMgr.count());

        Thread.sleep(3000);

        assertTrue(fired.get());
        assertEquals(0, timeMgr.count());
    }
}
