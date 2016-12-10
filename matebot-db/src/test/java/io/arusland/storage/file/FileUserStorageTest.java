package io.arusland.storage.file;

import io.arusland.storage.*;
import org.apache.commons.io.FileUtils;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static io.arusland.storage.file.TestUtils.assertNoneBlank;
import static junit.framework.TestCase.*;

/**
 * Created by ruslan on 07.12.2016.
 */
public class FileUserStorageTest {
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
    public void listRootItemsTest() {
        User user = new User(122342342L, "Maxi");
        UserStorage storage = getOrCreateStorage(user);
        List<Item> rootItems = storage.listItems("/");
        Set<ItemType> types = new HashSet<>();

        assertEquals(6, rootItems.size());

        for (Item item : rootItems) {
            assertNotNull(item.getType());
            assertTrue(ItemType.ROOT != item.getType());
            assertNoneBlank(item.getName());
            assertEquals(user, item.getUser());
            assertTrue(item.getFullPath().startsWith("/"));
            assertEquals(item.getName(), item.getFullPath().substring(1));
            assertEquals(0L, item.getSize());
            assertEquals("/", item.getParentPath());
            assertNotNull(item.tryGetFile());
            assertNotNull(item.getModifiedDate());

            types.add(item.getType());
        }

        assertEquals(6, types.size());

        List<String> names = rootItems.stream()
                .map(p -> p.getName())
                .collect(Collectors.toList());

        assertEquals(Arrays.asList("alerts", "audios", "docs", "images", "notes", "videos"),
                names);
    }

    @Test
    public void listRootItems_AreTheSameTest() {
        User user = new User(122342342L, "Maxi");
        UserStorage storage = getOrCreateStorage(user);
        List<Item> rootItems1 = storage.listItems("/");
        List<Item> rootItems2 = storage.listItems("/");

        assertEquals(6, rootItems1.size());
        assertSame(rootItems1, rootItems2);
    }

    @Test
    public void listRootItemsWithEmptyPath() {
        User user = new User(122342342L, "Maxi");
        UserStorage storage = getOrCreateStorage(user);
        List<Item> rootItems1 = storage.listItems("/");
        List<Item> rootItems2 = storage.listItems("");

        assertEquals(6, rootItems1.size());
        assertSame(rootItems1, rootItems2);
    }

    @Test
    public void getItemByRootIsNull() {
        User user = new User(122342342L, "Foo");
        UserStorage storage = getOrCreateStorage(user);
        Item item = storage.getItemByPath(ItemType.ROOT);

        assertNull(item);
    }

    @Test
    public void getItemByEmptyPathIsNull() {
        User user = new User(122342342L, "Foo");
        UserStorage storage = getOrCreateStorage(user);
        Item item = storage.getItemByPath("");

        assertNull(item);
    }

    @Test
    public void getItemByNullPathIsNull() {
        User user = new User(122342342L, "Foo");
        UserStorage storage = getOrCreateStorage(user);
        Item item = storage.getItemByPath((String) null);

        assertNull(item);
    }

    @Test
    public void getItemByType() {
        User user = new User(122342342L, "Foo");
        UserStorage storage = getOrCreateStorage(user);

        for (ItemType type : ItemType.values()) {
            if (type != ItemType.ROOT) {
                Item item = storage.getItemByPath(type);

                assertEquals(type, item.getType());
                assertEquals(type.normalized(), item.getName());
                assertTrue(item.getFullPath().startsWith("/"));
                assertEquals(type.normalized(), item.getFullPath().substring(1));
                assertEquals(user, item.getUser());
                assertEquals("/", item.getParentPath());
                assertNotNull(item.getModifiedDate());
                assertEquals(0, item.getSize());
                assertNotNull(item.tryGetFile());
                assertEquals("user" + user.getId(), item.tryGetFile().getParentFile().getName());

                assertTrue(item.listItems().isEmpty());
            }
        }
    }

    @Test
    public void twoUserStorage() {
        User user1 = new User(666426666L, "Mika");
        User user2 = new User(122342342L, "Maxi");
        UserStorage storage1 = getOrCreateStorage(user1);
        UserStorage storage2 = getOrCreateStorage(user2);

        List<Item> items1 = storage1.listItems("/");
        List<Item> items2 = storage2.listItems("/");

        assertEquals(items1.size(), items2.size());

        File file1 = items1.get(0).tryGetFile();
        File file2 = items2.get(0).tryGetFile();

        assertFalse(file1.equals(file2));
        assertEquals(file1.getParentFile().getParentFile(),
                file2.getParentFile().getParentFile());
    }

    @Test
    public void addFileTest() {
        User user = new User(122342342L, "Foo");
        UserStorage storage = getOrCreateStorage(user);
        File file1 = TestUtils.createTempFile(42);

        Item item = storage.addItem("/audios", "newname.mp3", file1);
        file1.delete();

        assertNotNull(item);
        assertEquals("newname.mp3", item.getName());
        assertEquals("/audios/newname.mp3", item.getFullPath());
        assertEquals(42, item.getSize());
        assertNotNull(item.tryGetFile());
        assertTrue(item.tryGetFile().exists());
    }

    @Test
    public void addFileIntoNestedDirTest() {
        User user = new User(122342342L, "Foo");
        UserStorage storage = getOrCreateStorage(user);
        File file1 = TestUtils.createTempFile(34);

        Item item = storage.addItem("/audios/my music", "newname4.mp3", file1);
        file1.delete();

        assertNotNull(item);
        assertEquals("newname4.mp3", item.getName());
        assertEquals("/audios/my music/newname4.mp3", item.getFullPath());
        assertEquals(34, item.getSize());
        assertNotNull(item.tryGetFile());
        assertTrue(item.tryGetFile().exists());
    }

    @Test
    public void addFileIntoRootDir() {
        User user = new User(122342342L, "Foo");
        UserStorage storage = getOrCreateStorage(user);
        File file1 = TestUtils.createTempFile(34);

        Item item = storage.addItem("/", "newname4.mp3", file1);
        file1.delete();

        assertNotNull(item);
        assertEquals("newname4.mp3", item.getName());
        assertEquals("/audios/newname4.mp3", item.getFullPath());
        assertEquals(34, item.getSize());
        assertNotNull(item.tryGetFile());
        assertTrue(item.tryGetFile().exists());
    }

    @Test
    public void autoAddingFileTest() {
        User user = new User(122342342L, "Foo");
        UserStorage storage = getOrCreateStorage(user);
        File file1 = TestUtils.createTempFile(37);

        Item item = storage.addItem("newname5.mp3", file1);
        file1.delete();

        assertNotNull(item);
        assertEquals("newname5.mp3", item.getName());
        assertEquals("/audios/newname5.mp3", item.getFullPath());
        assertEquals(37, item.getSize());
        assertNotNull(item.tryGetFile());
        assertTrue(item.tryGetFile().exists());

        file1 = TestUtils.createTempFile(40);

        item = storage.addItem("newname0.mp4", file1);
        file1.delete();

        assertNotNull(item);
        assertEquals("newname0.mp4", item.getName());
        assertEquals("/videos/newname0.mp4", item.getFullPath());
        assertEquals(40, item.getSize());
        assertNotNull(item.tryGetFile());
        assertTrue(item.tryGetFile().exists());

        file1 = TestUtils.createTempFile(13);

        item = storage.addItem("newname0.txt", file1);
        file1.delete();

        assertNotNull(item);
        assertEquals("newname0.txt", item.getName());
        assertEquals("/docs/newname0.txt", item.getFullPath());
        assertEquals(13, item.getSize());
        assertNotNull(item.tryGetFile());
        assertTrue(item.tryGetFile().exists());
    }

    @Test
    public void addFileIntoFileMustFail() {
        User user = new User(122342342L, "Foo");
        UserStorage storage = getOrCreateStorage(user);
        File file1 = TestUtils.createTempFile(41);

        try {
            Item item = storage.addItem("/audios", "newname.mp3", file1);
            file1.delete();

            assertNotNull(item);
            file1 = TestUtils.createTempFile(43);
            storage.addItem(item.getFullPath(), "another.mp4", file1);
            Assert.fail();
        } catch (StorageException ex) {
            assertTrue(ex.getMessage().startsWith("Cannot write file into file: "));
        } finally {
            file1.delete();
        }
    }

    @Test
    public void addFileWhenWithTheSameNameMustFail() {
        User user = new User(122342342L, "Foo");
        UserStorage storage = getOrCreateStorage(user);
        File file = TestUtils.createTempFile(41);

        try {
            Item item = storage.addItem("/audios", "newname.mp3", file);
            file.delete();

            assertNotNull(item);
            file = TestUtils.createTempFile(43);
            storage.addItem("/audios", "newname.mp3", file);
            Assert.fail();
        } catch (StorageException ex) {
            assertTrue(ex.getMessage().startsWith("File already exists: "));
        } finally {
            file.delete();
        }
    }

    @Test
    public void addingFileInTheWrongPlaceMustPutFileIntoRightPlace() {
        User user = new User(122342342L, "Foo");
        UserStorage storage = getOrCreateStorage(user);
        File file = TestUtils.createTempFile(49);

        Item item = storage.addItem("/docs", "newname9.avi", file);
        file.delete();

        assertNotNull(item);
        assertEquals("newname9.avi", item.getName());
        assertEquals("/videos/newname9.avi", item.getFullPath());
        assertEquals(49, item.getSize());
        assertNotNull(item.tryGetFile());
        assertTrue(item.tryGetFile().exists());
    }

    private UserStorage getOrCreateStorage(User user) {
        return (FileUserStorage) fileStorage.getOrCreate(user);
    }
}
