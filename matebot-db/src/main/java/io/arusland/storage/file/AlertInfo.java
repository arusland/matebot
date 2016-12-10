package io.arusland.storage.file;

import io.arusland.storage.util.DateValidator;

/**
 * Created by ruslan on 10.12.2016.
 */
public class AlertInfo {
    public final static int DAY_MONDAY = 0x01;
    public final static int DAY_TUESDAY = 0x02;
    public final static int DAY_WEDNESDAY = 0x04;
    public final static int DAY_THURSDAY = 0x08;
    public final static int DAY_FRIDAY = 0x10;
    public final static int DAY_SATURDAY = 0x20;
    public final static int DAY_SUNDAY = 0x40;
    public final int hour;
    public final int minute;
    public final Integer year;
    public final Integer month;
    public final Integer day;
    public final int weekDay;
    public final String message;
    public final boolean valid;

    private AlertInfo(int hour, int minute, Integer day, Integer month, Integer year, int weekDay, String message) {
        this.hour = hour;
        this.minute = minute;
        this.year = year;
        this.month = month;
        this.day = day;
        this.weekDay = weekDay;
        this.message = message != null ? message.trim() : "";
        this.valid = calcValid();
    }

    public AlertInfo(int hour, int minute, String message) {
        this(hour, minute, null, null, null, 0, message);
    }

    public AlertInfo(int hour, int minute, int weekDay, String message) {
        this(hour, minute, null, null, null, weekDay, message);
    }

    public AlertInfo(int hour, int minute, Integer day, Integer month, Integer year, String message) {
        this(hour, minute, day, month, year, 0, message);
    }

    private boolean calcValid() {
        if (hour < 0 || hour > 23 || minute > 59 || minute < 0) {
            return false;
        }

        return DateValidator.isValid(String.format("%2d/%2d/%4d",
                day != null ? day : 1, month != null ? month : 1, year != null ? year : 2000));
    }
}
