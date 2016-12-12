package io.arusland.bots.utils;

import org.apache.commons.lang3.Validate;

import java.util.*;

/**
 * Created by ruslan on 12.12.2016.
 */
public class TimeManagement implements AutoCloseable {
    private final Map<Date, Runnable> times = Collections.synchronizedMap(new IdentityHashMap<>());
    private final Timer timer = new Timer("MateBot timer", true);

    public void enqueue(Date time, Runnable handler) {
        Validate.notNull(time, "time");
        Validate.notNull(handler, "handler");

        times.put(time, handler);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Runnable handler = times.get(time);

                if (handler != null) {
                    times.remove(time);
                    handler.run();
                }
            }
        }, time);
    }

    public void dequeue(Date time) {
        if (time != null) {
            times.remove(time);
        }
    }

    @Override
    public void close() throws Exception {
        times.clear();
        timer.cancel();
    }

    public int count() {
        return times.size();
    }
}
