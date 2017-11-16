package io.arusland.storage.file;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;

/**
 * @author Ruslan Absalyamov
 * @since 2017-11-16
 */
public class AlertModelTest {
    @Test
    public void testFromFileJsonFormat() {
        AlertModel model = AlertModel.parseFile("{\"input\":\"12:24 hello!!!\"}");

        assertNotNull(model);
        assertEquals("12:24 hello!!!", model.getInput());
        assertNull(model.getLastActivePeriodTime());
    }

    @Test
    public void testFromFileJsonFormatWithLastActivePeriodTime() {
        AlertModel model = AlertModel.parseFile("{\"input\":\"12:24 hello!!!\", \"lastActivePeriodTime\": 1232132131231}");

        assertNotNull(model);
        assertEquals("12:24 hello!!!", model.getInput());
        assertEquals(1232132131231L, (long)model.getLastActivePeriodTime());
    }

    @Test
    public void testFromFileAlienJsonFormat() {
        AlertModel model = AlertModel.parseFile("{\"another\":\"12:24 hello!!!\"}");

        assertNotNull(model);
        assertEquals("", model.getInput());
        assertNull(model.getLastActivePeriodTime());
    }

    @Test
    public void testFromFileWithOldFormat() {
        AlertModel model = AlertModel.parseFile("14:18 21:10:2017 foo bar 32");

        assertNotNull(model);
        assertEquals("14:18 21:10:2017 foo bar 32", model.getInput());
        assertNull(model.getLastActivePeriodTime());
    }

    @Test
    public void testFromFileWhenFileEmpty() {
        AlertModel model = AlertModel.parseFile("");

        assertNotNull(model);
        assertEquals("", model.getInput());
        assertNull(model.getLastActivePeriodTime());
    }

    @Test
    public void testFromInputNull() {
        AlertModel model = AlertModel.parseFile(null);

        assertNotNull(model);
        assertEquals("", model.getInput());
        assertNull(model.getLastActivePeriodTime());
    }

    @Test
    public void testToFileString() {
        AlertModel model = new AlertModel("23:00 1-6 Go to sleep!", (Date)null);

        assertEquals("{\n  \"input\": \"23:00 1-6 Go to sleep!\"\n}", model.toFileString());
    }

    @Test
    public void testToFileStringWithLastActivePeriodTime() {
        AlertModel model = new AlertModel("23:00 1-6 Go to sleep!", 143254234632543L);

        assertEquals("{\n  \"input\": \"23:00 1-6 Go to sleep!\",\n  \"lastActivePeriodTime\": 143254234632543\n" +
                "}", model.toFileString());
    }

    @Test
    public void testToFileStringWithoutInit() {
        AlertModel model = new AlertModel();

        assertEquals("{}", model.toFileString());
    }
}
