package io.arusland.storage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by ruslan on 07.12.2016.
 */
public class TestUtils {
    public static void assertNoneBlank(String value) {
        assertTrue(StringUtils.isNoneBlank(value));
    }

    public static File createTempFile(int size) {
        try {
            File file = File.createTempFile("mtbot", null);
            FileUtils.write(file, RandomStringUtils.randomAscii(size), "UTF-8");

            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Calendar calcNextDayAfterTime(int hour, int minute) {
        Calendar now = Calendar.getInstance();
        int hourNow = now.get(Calendar.HOUR_OF_DAY);
        int minuteNow = now.get(Calendar.MINUTE);

        if (hourNow > hour || hourNow == hour && minuteNow > minute) {
            now.add(Calendar.HOUR_OF_DAY, 24);
        }

        now.set(Calendar.HOUR_OF_DAY, hour);
        now.set(Calendar.MINUTE, minute);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        return now;
    }
}
