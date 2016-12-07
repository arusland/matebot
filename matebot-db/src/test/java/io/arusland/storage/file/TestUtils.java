package io.arusland.storage.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

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
}
