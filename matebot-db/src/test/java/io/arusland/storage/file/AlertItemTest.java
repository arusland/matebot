package io.arusland.storage.file;

import io.arusland.storage.*;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sun.util.resources.cldr.ebu.CalendarData_ebu_KE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static io.arusland.storage.TestUtils.assertNoneBlank;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

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

        Item item = storage.addItem("/", "12:43");
        Calendar alertTime = TestUtils.calcNextDayAfterTime(12, 43);

        assertNotNull(item);
        assertTrue(item instanceof AlertItem);
        AlertItem alert = (AlertItem)item;
        assertEquals(ItemType.ALERTS, alert.getType());
        assertNoneBlank(alert.getName());
        assertTrue(alert.getName().endsWith(".alert"));
        assertEquals("/alerts/" + alert.getName(), alert.getFullPath());
        assertEquals("", alert.getMessage());
        assertEquals(alertTime.getTime(), alert.nextTime());
        assertEquals(String.format("12:43 %d:%d:2016", alertTime.get(Calendar.DAY_OF_MONTH),
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
        AlertItem alert = (AlertItem)item;
        assertEquals(ItemType.ALERTS, alert.getType());
        assertNoneBlank(alert.getName());
        assertTrue(alert.getName().endsWith(".alert"));
        assertEquals("/alerts/" + alert.getName(), alert.getFullPath());
        assertEquals("Go home, stop coding!", alert.getMessage());
        assertEquals(alertTime.getTime(), alert.nextTime());
        assertEquals(String.format("23:58 %d:%d:2016 Go home...", alertTime.get(Calendar.DAY_OF_MONTH),
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
            List<AlertItem> items = alertsRoot.listItems();

            assertEquals(2, items.size());
            assertTrue(items.get(0) instanceof AlertItem);
            assertTrue(items.get(1) instanceof AlertItem);
            // TODO: asserts!
        }
    }

    private UserStorage getOrCreateStorage(User user) {
        return (FileUserStorage) fileStorage.getOrCreate(user);
    }
}
