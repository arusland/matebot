package io.arusland.storage.file;

import io.arusland.storage.TimeZoneClient;
import io.arusland.storage.util.DateValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
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
    private static final Pattern ALERT_SHORT_PERIOD_PATTERN = Pattern.compile("^(\\d\\d*)(/\\d+)*:(\\d\\d*)(/\\d+)*( .*)*$");
    private static final Pattern ALERT_WEEK_PERIOD_PATTERN = Pattern.compile("^(\\d\\d*)(/\\d+)*:(\\d\\d*)(/\\d+)* ([\\d\\-,]+)( .*)*$");
    private static final Pattern ALERT_FULL_PERIOD_PATTERN3 = Pattern.compile("^(\\d\\d*)(/\\d+)*:(\\d\\d*)(/\\d+)* (\\d\\d*)(/\\d+)*:( .*)*$");
    private static final Pattern ALERT_FULL_PERIOD_PATTERN2 = Pattern.compile("^(\\d\\d*)(/\\d+)*:(\\d\\d*)(/\\d+)* (\\d\\d*)(/\\d+)*:(\\d\\d*)(/\\d+)*:*( .*)*$");
    private static final Pattern ALERT_FULL_PERIOD_PATTERN = Pattern.compile("^(\\d\\d*)(/\\d+)*:(\\d\\d*)(/\\d+)* (\\d\\d*)(/\\d+)*:(\\d\\d*)(/\\d+)*:(\\d{4})(/\\d+)*( .*)*$");
    private static final Pattern WEEK_DAY_PATTERN = Pattern.compile("^[1-7]$");
    private static final Pattern WEEK_DAY_PATTERN2 = Pattern.compile("^([1-7])-([1-7])$");
    private static final ChronoUnit[] PERIOD_TYPES = new ChronoUnit[]{ChronoUnit.HOURS, ChronoUnit.MINUTES,
            ChronoUnit.DAYS, ChronoUnit.MONTHS, ChronoUnit.YEARS};
    public final static int DAY_MONDAY = 0x01;
    public final static int DAY_TUESDAY = 0x02;
    public final static int DAY_WEDNESDAY = 0x04;
    public final static int DAY_THURSDAY = 0x08;
    public final static int DAY_FRIDAY = 0x10;
    public final static int DAY_SATURDAY = 0x20;
    public final static int DAY_SUNDAY = 0x40;
    public static final String PERIOD_CAN_BE_APPLIED_ONLY_ONCE = "Period can be applied only once";
    public final int hour;
    public final int minute;
    public final Integer year;
    public final Integer month;
    public final Integer day;
    public final int weekDays;
    public final String message;
    public final String content;
    public final Integer period;
    public final ChronoUnit periodType;
    public final boolean valid;
    public final String validMessage;

    private AlertInfo(int hour, int minute, Integer day, Integer month, Integer year,
                      int weekDays, Integer period, ChronoUnit periodType, String message,
                      String content) {
        this(hour, minute, day, month, year, weekDays, period, periodType, message, content, true, "");
    }

    private AlertInfo(int hour, int minute, Integer day, Integer month, Integer year,
                      int weekDays, Integer period, ChronoUnit periodType, String message,
                      String content, boolean valid, String validMessage) {
        this.hour = hour;
        this.minute = minute;
        this.year = year;
        this.month = month;
        this.day = day;
        this.weekDays = weekDays;
        this.message = message != null ? message.trim() : "";
        this.content = content;
        this.period = period;
        this.periodType = periodType;
        this.valid = valid;
        this.validMessage = validMessage;
    }

    public AlertInfo(int hour, int minute, String message, String content) {
        this(hour, minute, null, null, null, 0, null, null, message, content);
    }

    public AlertInfo(int hour, int minute, int weekDays, String message, String content) {
        this(hour, minute, null, null, null, weekDays, null, null, message, content);
    }

    public AlertInfo(int hour, int minute, int weekDays, Integer period, ChronoUnit periodType,
                     String message, String content) {
        this(hour, minute, null, null, null, weekDays, period, periodType, message, content);
    }

    public AlertInfo(int hour, int minute, Integer day, Integer month,
                     Integer year, String message, String content) {
        this(hour, minute, day, month, year, 0, null, null, message, content);
    }

    public AlertInfo(int hour, int minute, Integer day, Integer month,
                     Integer year, Integer period, ChronoUnit periodType, String message, String content) {
        this(hour, minute, day, month, year, 0, period, periodType, message, content);
    }

    public AlertInfo(int hour, int minute, Integer period, ChronoUnit periodType, String message, String content) {
        this(hour, minute, null, null, null, 0, period, periodType, message, content);
    }

    /**
     * Creates new instance of {@link AlertInfo}.
     */
    private AlertInfo setInvalidInfo(String validMessage) {
        return new AlertInfo(hour, minute, day, month, year, weekDays, period, periodType,
                message, content, false, validMessage);
    }

    /**
     * New instance of {@link AlertInfo} might be created.
     */
    private AlertInfo validate() {
        if (hour < 0 || hour > 23 || minute > 59 || minute < 0) {
            return setInvalidInfo("");
        }

        if (day == null && month == null && year == null) {
            return this;
        }

        boolean isValid = DateValidator.isValid(day, month, year);

        if (!isValid) {
            // TODO: set information about invalid date
            return setInvalidInfo("");
        }

        return this;
    }

    public static AlertInfo parse(final String input, TimeZoneClient timeZoneClient) {
        if (StringUtils.isBlank(input)) {
            return null;
        }

        AlertInfo info = parseInternal(input.trim());

        if (info != null) {
            info = info.validate();
        }

        return normalize(info, timeZoneClient);
    }

    /**
     * Normalize fields (year, month, day) of {@link AlertInfo}
     * when they all are <code>null</code>.
     *
     * @param info           input instance.
     * @param timeZoneClient Client time converter.
     */
    private static AlertInfo normalize(final AlertInfo info, TimeZoneClient timeZoneClient) {
        if (info == null || !info.valid || StringUtils.isNotBlank(info.validMessage)) {
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
            String content;

            if (info.periodType != null) {
                if (info.periodType == ChronoUnit.HOURS) {
                    content = String.format("%d/%d:%d %d:%d:%d",
                            info.hour, info.period, info.minute, day, month, year);
                } else {
                    content = String.format("%d:%d/%d %d:%d:%d",
                            info.hour, info.minute, info.period, day, month, year);
                }
            } else {
                content = String.format("%d:%d %d:%d:%d",
                        info.hour, info.minute, day, month, year);
            }


            if (StringUtils.isNotBlank(info.message)) {
                content += " " + info.message;
            }

            return new AlertInfo(info.hour, info.minute, day,
                    month, year, info.period, info.periodType, info.message, content);
        }

        return info;
    }

    private static AlertInfo parseInternal(final String input) {
        Matcher mc = ALERT_FULL_PATTERN.matcher(input);

        if (mc.find()) {
            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)), Integer.parseInt(mc.group(3)),
                    Integer.parseInt(mc.group(4)), Integer.parseInt(mc.group(5)), mc.group(6), input);
        }

        mc = ALERT_FULL_PERIOD_PATTERN.matcher(input);

        if (mc.find()) {
            List<String> periods = Arrays.asList(mc.group(2), mc.group(4), mc.group(6), mc.group(8), mc.group(10));

            if (periods.stream().filter(StringUtils::isNoneBlank).count() > 1) {
                return AlertInfo.invalidAlertInfo(PERIOD_CAN_BE_APPLIED_ONLY_ONCE);
            }

            PeriodInfo pinfo = getPeriodInfo(periods);

            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(3)), Integer.parseInt(mc.group(5)),
                    Integer.parseInt(mc.group(7)), Integer.parseInt(mc.group(9)), pinfo.period, pinfo.type, mc.group(11), input);
        }

        mc = ALERT_FULL_PATTERN2.matcher(input);

        if (mc.find()) {
            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)), Integer.parseInt(mc.group(3)),
                    Integer.parseInt(mc.group(4)), (Integer) null, mc.group(5), input);
        }

        mc = ALERT_FULL_PERIOD_PATTERN2.matcher(input);

        if (mc.find()) {
            List<String> periods = Arrays.asList(mc.group(2), mc.group(4), mc.group(6), mc.group(8));

            if (periods.stream().filter(StringUtils::isNoneBlank).count() > 1) {
                return AlertInfo.invalidAlertInfo(PERIOD_CAN_BE_APPLIED_ONLY_ONCE);
            }

            PeriodInfo pinfo = getPeriodInfo(periods);

            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(3)), Integer.parseInt(mc.group(5)),
                    Integer.parseInt(mc.group(7)), (Integer) null, pinfo.period, pinfo.type, mc.group(9), input);
        }

        mc = ALERT_FULL_PATTERN3.matcher(input);

        if (mc.find()) {
            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)), Integer.parseInt(mc.group(3)),
                    (Integer) null, (Integer) null, mc.group(4), input);
        }

        mc = ALERT_FULL_PERIOD_PATTERN3.matcher(input);

        if (mc.find()) {
            List<String> periods = Arrays.asList(mc.group(2), mc.group(4), mc.group(6));

            if (periods.stream().filter(StringUtils::isNoneBlank).count() > 1) {
                return AlertInfo.invalidAlertInfo(PERIOD_CAN_BE_APPLIED_ONLY_ONCE);
            }

            PeriodInfo pinfo = getPeriodInfo(periods);

            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(3)), Integer.parseInt(mc.group(5)),
                    (Integer) null, (Integer) null, pinfo.period, pinfo.type, mc.group(7), input);
        }

        mc = ALERT_WEEK_PATTERN.matcher(input);

        if (mc.find()) {
            int flags = parseFlags(mc.group(3));

            if (flags > 0) {
                return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)),
                        flags, mc.group(4), input);
            }
        }

        mc = ALERT_WEEK_PERIOD_PATTERN.matcher(input);

        if (mc.find()) {
            int flags = parseFlags(mc.group(5));

            if (flags > 0) {
                List<String> periods = Arrays.asList(mc.group(2), mc.group(4));

                if (periods.stream().filter(StringUtils::isNoneBlank).count() > 1) {
                    return AlertInfo.invalidAlertInfo(PERIOD_CAN_BE_APPLIED_ONLY_ONCE);
                }

                PeriodInfo pinfo = getPeriodInfo(periods);
                return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(3)),
                        flags, pinfo.period, pinfo.type, mc.group(6), input);
            }
        }

        mc = ALERT_SHORT_PATTERN.matcher(input);

        if (mc.find()) {
            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)),
                    mc.group(3), input);
        }

        mc = ALERT_SHORT_PERIOD_PATTERN.matcher(input);

        if (mc.find()) {
            List<String> periods = Arrays.asList(mc.group(2), mc.group(4));

            if (periods.stream().filter(StringUtils::isNoneBlank).count() > 1) {
                return AlertInfo.invalidAlertInfo(PERIOD_CAN_BE_APPLIED_ONLY_ONCE);
            }

            PeriodInfo pinfo = getPeriodInfo(periods);
            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(3)),
                    pinfo.period, pinfo.type, mc.group(5), input);
        }

        return null;
    }

    private static PeriodInfo getPeriodInfo(List<String> periods) {
        for (int i = 0; i < periods.size(); i++) {
            if (StringUtils.isNotBlank(periods.get(i))) {
                int period = Integer.parseInt(periods.get(i).substring(1));
                return new PeriodInfo(period, PERIOD_TYPES[i]);
            }
        }

        throw new IllegalStateException("Expected peiod");
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
                ", period=" + period +
                ", periodType=" + periodType +
                ", valid=" + valid +
                '}';
    }

    public static AlertInfo invalidAlertInfo(String validMessage) {
        Validate.notBlank(validMessage, "validMessage");

        return new AlertInfo(0, 0, null, null, null, 0, null, null, null, null, false, validMessage);
    }

    private static class ValidInfo {
        final boolean valid;
        final String validMessage;

        public ValidInfo(boolean valid) {
            this(valid, "");
        }

        public ValidInfo(boolean valid, String validMessage) {
            this.valid = valid;
            this.validMessage = validMessage;
        }

        static final ValidInfo VALID = new ValidInfo(true);
        static final ValidInfo INVALID = new ValidInfo(false);
    }

    private static class PeriodInfo {
        final int period;
        final ChronoUnit type;

        public PeriodInfo(int period, ChronoUnit type) {
            this.period = period;
            this.type = type;
        }
    }
}
