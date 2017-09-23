package io.arusland.storage.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ruslan on 11.12.2016.
 */
public final class DateValidator {
    private final static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    static {
        sdf.setLenient(false);
    }

    /**
     * Checks if date is valid.
     */
    public static boolean isValid(Integer day, Integer month, Integer year) {
        if (day == null && month == null && year == null) {
            return false;
        }

        return isValid(String.format("%2d/%2d/%4d",
                day != null ? day : 1, month != null ? month : 1, year != null ? year : 2000));
    }

    /**
     * Checks if date is valid.
     *
     * @param dateToValidate Input date in format <code>dd/MM/yyyy</code>.
     */
    public synchronized static boolean isValid(String dateToValidate) {
        try {
            //if not valid, it will throw ParseException
            Date date = sdf.parse(dateToValidate);
            return date != null;
        } catch (ParseException e) {
            return false;
        }
    }
}
