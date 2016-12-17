package io.arusland.bots.utils;

import java.util.Date;

/**
 * Created by ruslan on 12.12.2016.
 */
public class TimeUtils {
    public static String friendlyTimespan(Date date) {
        long now = date.getTime() - System.currentTimeMillis();

        if (now > 0) {
            int sec = (int) (now / 1000);

            if (sec == 0) {
                return "several seconds";
            }

            int mins = sec / 60;

            if (mins == 0) {
                return sec + " seconds";
            }

            int hours = mins / 60;

            if (hours == 0) {
                return mins + " minutes";
            }

            mins = mins % 60;
            int days = hours / 24;

            if (days == 0) {
                String res = hours + " hours";

                if (mins > 0) {
                    res += " " + mins + " minutes";
                }

                return res;
            }

            int months = days / 30;
            hours = hours % 60;

            if (months == 0) {
                String res = days + " days";

                if (hours > 0) {
                    res += " " + hours + " hours";
                }

                return res;
            }

            return months + " months (" + days + " days)";
        }

        return "";
    }
}
