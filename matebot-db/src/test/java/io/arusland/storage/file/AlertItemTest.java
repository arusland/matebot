package io.arusland.storage.file;

import io.arusland.storage.*;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static io.arusland.storage.TestUtils.assertNoneBlank;
import static junit.framework.TestCase.*;

/**
 * Created by ruslan on 14.12.2016.
 */
public class AlertItemTest {
    private Path root;
    private FileStorage fileStorage;

    @Before
    public void beforeEachTest() throws IOException {
        root = Files.createTempDirectory("matebot");
        fileStorage = new FileStorage(root.toString(), Collections.emptyMap());
    }

    @After
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
        assertEquals(String.format("02:43 %02d:%02d:2016", alertTime.get(Calendar.DAY_OF_MONTH),
                alertTime.get(Calendar.MONTH) + 1), alert.getTitle());
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
        assertEquals(String.format("23:58 %02d:%02d:2016 Go home...", alertTime.get(Calendar.DAY_OF_MONTH),
                alertTime.get(Calendar.MONTH) + 1), alert.getTitle());
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

    private UserStorage getOrCreateStorage(User user) {
        return (FileUserStorage) fileStorage.getOrCreate(user);
    }
}
