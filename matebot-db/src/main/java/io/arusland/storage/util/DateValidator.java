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
     *
     * @param dateToValidate Input date in format <code>dd/MM/yyyy</code>.
     * @return
     */
    public static boolean isValid(String dateToValidate) {
        try {
            //if not valid, it will throw ParseException
            Date date = sdf.parse(dateToValidate);
            return date != null;
        } catch (ParseException e) {
            return false;
        }
    }
}
