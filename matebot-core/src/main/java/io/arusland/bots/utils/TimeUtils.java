package io.arusland.bots.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by ruslan on 12.12.2016.
 */
public class TimeUtils {
    public static String friendlyTimespan(Date date) {
        return friendlyTimespan(date, System.currentTimeMillis());
    }

    protected static String friendlyTimespan(Date date, long currentTimeMS) {
        long now = date.getTime() - currentTimeMS;

        if (now >= 0) {
            int sec = (int) (now / 1000);

            if (sec == 0) {
                return "few moments";
            }

            int mins = sec / 60;

            if (mins == 0) {
                return formatValue(sec, "second");
            }

            int hours = mins / 60;

            if (hours == 0) {
                return formatValue(mins, "minute");
            }

            mins = mins % 60;
            int days = hours / 24;

            if (days == 0) {
                String res = formatValue(hours, "hour");

                if (mins > 0) {
                    res += " " + formatValue(mins, "minute");
                }

                return res;
            }

            int months = days / 30;
            hours = hours % 24;

            if (months == 0) {
                String res = formatValue(days, "day");

                if (hours > 0) {
                    res += " " + formatValue(hours, "hour");
                }

                return res;
            }

            int years = days / 365;

            if (years == 0) {
                return formatValue(months, "month") + " (" + formatValue(days, "day") + ")";
            }

            String res = formatValue(years, "year");
            months %= 12;

            if (months == 0) {
                days %= 365;

                if (days > 0) {
                    res += " " + formatValue(days, "day");
                }

                return res;
            } else {
                return res + " " + formatValue(months, "month");
            }
        }

        return "";
    }

    public static String friendlyTimespanShort(Date nextTime) {
        return friendlyTimespanShort(nextTime, System.currentTimeMillis());
    }

    public static String friendlyTimespanShort(Date nextTime, long currentTimeMS) {
        return friendlyTimespan(nextTime, currentTimeMS)
                .replaceAll("hours?", "h.")
                .replaceAll("minutes?", "m.")
                .replaceAll("seconds?", "s.")
                .replaceAll("days?", "d.")
                .replaceAll("several", "")
                .replaceAll("months?", "mon.")
                .replaceAll("years?", "y.")
                .replaceAll("  +", " ");
    }

    public static Date getTodayEnd(Date now) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.set(Calendar.MILLISECOND, 999);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(cal.SECOND, 59);

        return cal.getTime();
    }

    public static Date getWeekEnd(Date now) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getTodayEnd(now));

        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return cal.getTime();
    }

    public static Date getMonthEnd(Date now) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getTodayEnd(now));
        Date lastDayEnd = cal.getTime();

        while (cal.get(Calendar.DAY_OF_MONTH) != 1) {
            lastDayEnd = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return lastDayEnd;
    }

    private static String formatValue(int value, String type) {
        return String.format("%d %s", value, ifPlural(value, type));
    }

    private static String ifPlural(int value, String type) {
        return value > 1 ? type + "s" : type;
    }
}
