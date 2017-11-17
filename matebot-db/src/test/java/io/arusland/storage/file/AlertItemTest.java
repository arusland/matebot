package io.arusland.storage.file;

import com.google.gson.Gson;
import io.arusland.storage.*;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static io.arusland.storage.TestUtils.assertNoneBlank;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Created by ruslan on 14.12.2016.
 */
public class AlertItemTest {
    private final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private Path root;
    private FileStorage fileStorage;

    @BeforeEach
    public void beforeEachTest() throws IOException {
        root = Files.createTempDirectory("matebot");
        fileStorage = new FileStorage(root.toString(), Collections.emptyMap());

        FileAlertItem.configure(() -> new Date());
        AlertInfo.configure(() -> new Date());
    }

    @AfterEach
    public void afterEachTest() throws IOException {
        FileUtils.deleteDirectory(root.toFile());
    }

    @Test
    public void testAddingAlertItemWithoutMessage() {
        User user = new User(1123581321L, "Foo");
        UserStorage storage = getOrCreateStorage(user);

        Item item = storage.addItem("/", "2:43");
        Calendar alertTime = TestUtils.calcNextDayAfterTime(2, 43);

        assertNotNull(item);
        assertTrue(item instanceof AlertItem);
        AlertItem alert = (AlertItem) item;
        assertEquals(ItemType.ALERTS, alert.getType());
        assertNoneBlank(alert.getName());
        assertTrue(alert.getName().endsWith(".alert"));
        assertEquals("/alerts/" + alert.getName(), alert.getFullPath());
        assertEquals("", alert.getMessage());
        assertEquals(alertTime.getTime(), alert.nextTime());
        assertEquals(String.format("02:43 %02d:%02d:%d", alertTime.get(Calendar.DAY_OF_MONTH),
                alertTime.get(Calendar.MONTH) + 1, alertTime.get(Calendar.YEAR)), alert.getTitle());
    }

    @Test
    public void testAddingAlertItemWithMessage() {
        User user = new User(1123581321L, "Foo");
        UserStorage storage = getOrCreateStorage(user);

        Item item = storage.addItem("/", "23:58 Go home, stop coding!");
        Calendar alertTime = TestUtils.calcNextDayAfterTime(23, 58);

        assertNotNull(item);
        assertTrue(item instanceof AlertItem);
        AlertItem alert = (AlertItem) item;
        assertEquals(ItemType.ALERTS, alert.getType());
        assertNoneBlank(alert.getName());
        assertTrue(alert.getName().endsWith(".alert"));
        assertEquals("/alerts/" + alert.getName(), alert.getFullPath());
        assertEquals("Go home, stop coding!", alert.getMessage());
        assertEquals(alertTime.getTime(), alert.nextTime());
        assertEquals(String.format("23:58 %02d:%02d:%d Go home, stop coding!", alertTime.get(Calendar.DAY_OF_MONTH),
                alertTime.get(Calendar.MONTH) + 1, alertTime.get(Calendar.YEAR)), alert.getTitle());
    }

    @Test
    public void testAddingMultipleAlertItems() {
        User user = new User(1123581321L, "Foo");

        {
            UserStorage storage = getOrCreateStorage(user);

            AlertItem item1 = (AlertItem) storage.addItem("/alerts", "0:14 Go to bed!");
            AlertItem item2 = (AlertItem) storage.addItem("/", "06:30 Ты можешь еще поспать!");

            assertNotNull(item1);
            assertNotNull(item2);
        }

        {
            UserStorage storage = getOrCreateStorage(user);

            Item alertsRoot = storage.getItemByPath(ItemType.ALERTS);
            List<AlertItem> alerts = alertsRoot.listItems();

            assertEquals(2, alerts.size());

            for (AlertItem alert1 : alerts) {
                assertEquals(ItemType.ALERTS, alert1.getType());
                assertNoneBlank(alert1.getName());
                assertTrue(alert1.getName().endsWith(".alert"));
                assertEquals("/alerts/" + alert1.getName(), alert1.getFullPath());
            }

            assertEquals("Go to bed!", alerts.get(0).getMessage());
            assertEquals("Ты можешь еще поспать!", alerts.get(1).getMessage());
        }
    }

    @Test
    public void testFailWhenWhenMultiplePeriodAdded() {
        User user = new User(1123581321L, "Foo");
        UserStorage storage = getOrCreateStorage(user);

        StorageException ex = assertThrows(StorageException.class, () -> storage.addItem("/", "23/2:58/1 Go home, stop coding!"));

        assertEquals("Period can be applied only once", ex.getMessage());
    }

    @Test
    public void testAddingAlertItemWithPeriod() throws ParseException, IOException {
        User user = new User(1123581321L, "Foo");
        UserStorage storage = getOrCreateStorage(user);
        AtomicLong currentTime = new AtomicLong();
        currentTime.set(DF.parse("2016-05-12 12:45:36.123").getTime());

        FileAlertItem.configure(() -> new Date(currentTime.get()));
        AlertInfo.configure(() -> new Date(currentTime.get()));

        FileAlertItem alert = (FileAlertItem) storage.addItem("/", "23:58/2 Go home, stop coding!");

        assertTrue(alert.isActive());
        assertNotNull(alert.nextTime());
        assertEquals("2016-05-12 23:58:00.000", DF.format(alert.nextTime()));
        assertEquals("2016-05-13 00:00:00.000", DF.format(alert.getLastActivePeriodTime()));

        AlertModel model = getAlertModelFromFile(alert.getFile());

        assertEquals("23:58/2 12:5:2016 Go home, stop coding!", model.getInput());
        assertEquals("2016-05-13 00:00:00.000", DF.format(model.getLastActivePeriodTimeDate()));
    }

    private static AlertModel getAlertModelFromFile(File file) throws IOException {
        String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

        return new Gson().fromJson(content, AlertModel.class);
    }

    @Test
    public void testLastActivePeriodTimeWithShortFormat() throws ParseException, IOException {
        User user = new User(1123581321L, "Foo");
        UserStorage storage = getOrCreateStorage(user);
        AtomicLong currentTime = new AtomicLong();
        currentTime.set(DF.parse("2016-05-12 12:45:36.123").getTime());

        FileAlertItem.configure(() -> new Date(currentTime.get()));
        AlertInfo.configure(() -> new Date(currentTime.get()));

        FileAlertItem alert = (FileAlertItem) storage.addItem("/", "23:58/3 Go home, stop coding!");

        assertTrue(alert.isActive());
        assertFalse(alert.isPeriodActive());
        assertNotNull(alert.nextTime());
        assertEquals("2016-05-12 23:58:00.000", DF.format(alert.nextTime()));
        assertEquals("2016-05-13 00:01:00.000", DF.format(alert.getLastActivePeriodTime()));

        AlertModel model = getAlertModelFromFile(alert.getFile());
        assertEquals("2016-05-13 00:01:00.000", DF.format(model.getLastActivePeriodTimeDate()));

        currentTime.set(DF.parse("2016-05-13 00:00:00.499").getTime());

        assertTrue(alert.isActive());
        assertTrue(alert.isPeriodActive());
        assertEquals("2016-05-13 00:01:00.000", DF.format(alert.nextTime()));
        assertEquals("2016-05-13 00:01:00.000", DF.format(alert.getLastActivePeriodTime()));

        currentTime.set(DF.parse("2016-05-13 00:03:34.499").getTime());

        assertTrue(alert.isActive());
        assertTrue(alert.isPeriodActive());
        assertEquals("2016-05-13 00:04:00.000", DF.format(alert.nextTime()));
        assertEquals("2016-05-13 00:04:00.000", DF.format(alert.getLastActivePeriodTime()));

        model = getAlertModelFromFile(alert.getFile());
        assertEquals("2016-05-13 00:04:00.000", DF.format(model.getLastActivePeriodTimeDate()));

        alert.cancelActivePeriod();

        assertFalse(alert.isActive());
        assertFalse(alert.isPeriodActive());
        assertEquals("2016-05-12 23:58:00.000", DF.format(alert.nextTime()));
        assertNull(alert.getLastActivePeriodTime());

        model = getAlertModelFromFile(alert.getFile());
        assertNull(model.getLastActivePeriodTimeDate());
    }

    @Test
    public void testLastActivePeriodTimeWithWeekDays() throws ParseException, IOException {
        User user = new User(1123581321L, "Foo");
        UserStorage storage = getOrCreateStorage(user);
        AtomicLong currentTime = new AtomicLong();
        currentTime.set(DF.parse("2016-05-14 12:45:36.123").getTime());

        FileAlertItem.configure(() -> new Date(currentTime.get()));
        AlertInfo.configure(() -> new Date(currentTime.get()));

        FileAlertItem alert = (FileAlertItem) storage.addItem("/", "23:58/4 2-5 Blockchain!!");

        assertTrue(alert.isActive());
        assertFalse(alert.isPeriodActive());
        assertNotNull(alert.nextTime());
        assertEquals("2016-05-17 23:58:00.000", DF.format(alert.nextTime()));
        assertEquals("2016-05-18 00:02:00.000", DF.format(alert.getLastActivePeriodTime()));

        AlertModel model = getAlertModelFromFile(alert.getFile());
        assertEquals("2016-05-18 00:02:00.000", DF.format(model.getLastActivePeriodTimeDate()));

        currentTime.set(DF.parse("2016-05-18 00:00:44.499").getTime());

        assertTrue(alert.isActive());
        assertTrue(alert.isPeriodActive());
        assertEquals("2016-05-18 00:02:00.000", DF.format(alert.nextTime()));
        assertEquals("2016-05-18 00:02:00.000", DF.format(alert.getLastActivePeriodTime()));

        currentTime.set(DF.parse("2016-05-18 00:04:34.499").getTime());

        assertTrue(alert.isActive());
        assertTrue(alert.isPeriodActive());
        assertEquals("2016-05-18 00:06:00.000", DF.format(alert.nextTime()));
        assertEquals("2016-05-18 00:06:00.000", DF.format(alert.getLastActivePeriodTime()));

        model = getAlertModelFromFile(alert.getFile());
        assertEquals("2016-05-18 00:06:00.000", DF.format(model.getLastActivePeriodTimeDate()));

        currentTime.set(DF.parse("2016-05-19 23:57:34.499").getTime());

        assertTrue(alert.isActive());
        assertFalse(alert.isPeriodActive());
        assertEquals("2016-05-19 23:58:00.000", DF.format(alert.nextTime()));
        assertEquals("2016-05-20 00:02:00.000", DF.format(alert.getLastActivePeriodTime()));

        model = getAlertModelFromFile(alert.getFile());
        assertEquals("2016-05-20 00:02:00.000", DF.format(model.getLastActivePeriodTimeDate()));

        currentTime.set(DF.parse("2016-05-20 00:01:34.499").getTime());

        assertTrue(alert.isActive());
        assertTrue(alert.isPeriodActive());
        assertEquals("2016-05-20 00:02:00.000", DF.format(alert.nextTime()));
        assertEquals("2016-05-20 00:02:00.000", DF.format(alert.getLastActivePeriodTime()));

        alert.cancelActivePeriod();

        assertTrue(alert.isActive());
        assertFalse(alert.isPeriodActive());
        assertEquals("2016-05-20 23:58:00.000", DF.format(alert.nextTime()));
        assertEquals("2016-05-21 00:02:00.000", DF.format(alert.getLastActivePeriodTime()));

        model = getAlertModelFromFile(alert.getFile());
        assertEquals("2016-05-21 00:02:00.000", DF.format(model.getLastActivePeriodTimeDate()));
    }

    private UserStorage getOrCreateStorage(User user) {
        return (FileUserStorage) fileStorage.getOrCreate(user);
    }
}
