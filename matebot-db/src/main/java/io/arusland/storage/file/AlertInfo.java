package io.arusland.storage.file;

import io.arusland.storage.TimeZoneClient;
import io.arusland.storage.util.DateValidator;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Alert format parser and fields holder.
 * <p>
 * Created by ruslan on 10.12.2016.
 */
public class AlertInfo {
    private static final Pattern ALERT_FULL_PATTERN = Pattern.compile("^(\\d\\d*):(\\d\\d*) (\\d\\d*):(\\d\\d*):(\\d{4})( .*)*$");
    private static final Pattern ALERT_FULL_PATTERN2 = Pattern.compile("^(\\d\\d*):(\\d\\d*) (\\d\\d*):(\\d\\d*):*( .*)*$");
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
    public final int weekDays;
    public final String message;
    public final String content;
    public final boolean valid;

    private AlertInfo(int hour, int minute, Integer day, Integer month, Integer year,
                      int weekDays, String message, String content) {
        this.hour = hour;
        this.minute = minute;
        this.year = year;
        this.month = month;
        this.day = day;
        this.weekDays = weekDays;
        this.message = message != null ? message.trim() : "";
        this.valid = calcValid();
        this.content = content;
    }

    public AlertInfo(int hour, int minute, String message, String content) {
        this(hour, minute, null, null, null, 0, message, content);
    }

    public AlertInfo(int hour, int minute, int weekDays, String message, String content) {
        this(hour, minute, null, null, null, weekDays, message, content);
    }

    public AlertInfo(int hour, int minute, Integer day, Integer month,
                     Integer year, String message, String content) {
        this(hour, minute, day, month, year, 0, message, content);
    }

    private boolean calcValid() {
        if (hour < 0 || hour > 23 || minute > 59 || minute < 0) {
            return false;
        }

        if (day == null && month == null && year == null) {
            return true;
        }

        return DateValidator.isValid(day, month, year);
    }

    public static AlertInfo parse(final String input, TimeZoneClient timeZoneClient) {
        if (StringUtils.isBlank(input)) {
            return null;
        }

        AlertInfo info = parseInternal(input.trim());

        return normalize(info, timeZoneClient);
    }

    /**
     * Normazlize fields (year, month, day) of {@link AlertInfo}
     * when they all are <code>null</code>.
     *
     * @param info           input instance.
     * @param timeZoneClient Client time converter.
     */
    private static AlertInfo normalize(final AlertInfo info, TimeZoneClient timeZoneClient) {
        if (info == null || !info.valid) {
            return info;
        }

        if (info.weekDays == 0 &&
                info.year == null && info.month == null && info.day == null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(timeZoneClient.toClient(cal.getTime()));
            long currentMillis = cal.getTimeInMillis();
            cal.set(Calendar.HOUR_OF_DAY, info.hour);
            cal.set(Calendar.MINUTE, info.minute);

            // we have to alert tomorrow at specified time
            if (cal.getTimeInMillis() < currentMillis) {
                cal.add(Calendar.HOUR_OF_DAY, 24);
            }

            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DATE);
            String content = String.format("%d:%d %d:%d:%d",
                    info.hour, info.minute, day, month, year);

            if (StringUtils.isNotBlank(info.message)) {
                content += " " + info.message;
            }

            return new AlertInfo(info.hour, info.minute, day,
                    month, year, info.message, content);
        }

        return info;
    }

    private static AlertInfo parseInternal(final String input) {
        Matcher mc = ALERT_FULL_PATTERN.matcher(input);

        if (mc.find()) {
            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)), Integer.parseInt(mc.group(3)),
                    Integer.parseInt(mc.group(4)), Integer.parseInt(mc.group(5)), mc.group(6), input);
        }

        mc = ALERT_FULL_PATTERN2.matcher(input);

        if (mc.find()) {
            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)), Integer.parseInt(mc.group(3)),
                    Integer.parseInt(mc.group(4)), null, mc.group(5), input);
        }

        mc = ALERT_FULL_PATTERN3.matcher(input);

        if (mc.find()) {
            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)), Integer.parseInt(mc.group(3)),
                    null, null, mc.group(4), input);
        }

        mc = ALERT_WEEK_PATTERN.matcher(input);

        if (mc.find()) {
            int flags = parseFlags(mc.group(3));

            if (flags > 0) {
                return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)),
                        flags, mc.group(4), input);
            }
        }

        mc = ALERT_SHORT_PATTERN.matcher(input);

        if (mc.find()) {
            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)),
                    mc.group(3), input);
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

    @Override
    public String toString() {
        return "AlertInfo{" +
                "hour=" + hour +
                ", minute=" + minute +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", weekDays=" + weekDays +
                ", message='" + message + '\'' +
                ", content='" + content + '\'' +
                ", valid=" + valid +
                '}';
    }
}
