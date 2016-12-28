package io.arusland.matebot.web;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ruslan on 29.12.2016.
 */
public class Statistics {
    private final static AtomicLong pageViewCount = new AtomicLong(0);

    public static long incrementPageViewCount() {
        return pageViewCount.incrementAndGet();
    }

    public static long pageViewCount() {
        return pageViewCount.get();
    }
}
