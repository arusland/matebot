package io.arusland.storage.file;

import io.arusland.storage.util.DateValidator;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ruslan on 10.12.2016.
 */
public class AlertInfo {
    private static final Pattern ALERT_FULL_PATTERN = Pattern.compile("^(\\d\\d*):(\\d\\d*) (\\d\\d*):(\\d\\d*):(\\d{4})( .*)*$");
    private static final Pattern ALERT_FULL_PATTERN2 = Pattern.compile("^(\\d\\d*):(\\d\\d*) (\\d\\d*):(\\d\\d*)( .*)*$");
    private static final Pattern ALERT_FULL_PATTERN3 = Pattern.compile("^(\\d\\d*):(\\d\\d*) (\\d\\d*):( .*)*$");
    private static final Pattern ALERT_SHORT_PATTERN = Pattern.compile("^(\\d\\d*):(\\d\\d*)( .*)*$");
    private static final Pattern ALERT_WEEK_PATTERN = Pattern.compile("^(\\d\\d*):(\\d\\d*) ([\\d\\-,]+)( .*)*$");
    private static final Pattern WEEK_DAY_PATTERN = Pattern.compile("^[1-7]$");
    private static final Pattern WEEK_DAY_PATTERN2 = Pattern.compile("^([1-7])-([1-7])$");
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

    public static AlertInfo parse(final String input0) {
        if (StringUtils.isBlank(input0)) {
            return null;
        }

        final String input = input0.trim();

        Matcher mc = ALERT_FULL_PATTERN.matcher(input);

        if (mc.find()) {
            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)), Integer.parseInt(mc.group(3)),
                    Integer.parseInt(mc.group(4)), Integer.parseInt(mc.group(5)), mc.group(6));
        }

        mc = ALERT_FULL_PATTERN2.matcher(input);

        if (mc.find()) {
            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)), Integer.parseInt(mc.group(3)),
                    Integer.parseInt(mc.group(4)), null, mc.group(5));
        }

        mc = ALERT_FULL_PATTERN3.matcher(input);

        if (mc.find()) {
            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)), Integer.parseInt(mc.group(3)),
                    null, null, mc.group(4));
        }

        mc = ALERT_WEEK_PATTERN.matcher(input);

        if (mc.find()) {
            int flags = parseFlags(mc.group(3));

            if (flags > 0) {
                return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)), flags, mc.group(4));
            }
        }

        mc = ALERT_SHORT_PATTERN.matcher(input);

        if (mc.find()) {
            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)), mc.group(3));
        }

        return null;
    }

    /**
     * Convert week days to flags representation
     * <p>
     * 6   => 00100000b<br>
     * 1-5 => 00011111b<br>
     * 1,6-7 => 01100001b<br>
     */
    private static int parseFlags(String input) {
        List<Boolean> errors = new ArrayList<>();
        List<Integer> flags = new ArrayList<>();

        Arrays.stream(input.split(",")).forEach(s -> {
            if (WEEK_DAY_PATTERN.matcher(s).find()) {
                flags.add(1 << (Integer.parseInt(s) - 1));
            } else {
                Matcher mc = WEEK_DAY_PATTERN2.matcher(s);

                if (mc.find()) {
                    int start = Integer.parseInt(mc.group(1));
                    int end = Integer.parseInt(mc.group(2));

                    if (start <= end) {
                        for (int i = start; i <= end; i++) {
                            flags.add(1 << (i - 1));
                        }
                    } else {
                        errors.add(true);
                    }
                } else {
                    errors.add(true);
                }
            }
        });

        if (errors.isEmpty()) {
            return flags.stream().reduce(0, (flg, next) -> flg |= next);
        }

        return 0;
    }
}