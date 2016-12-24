package io.arusland.storage.file;

import io.arusland.storage.*;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static io.arusland.storage.TestUtils.assertNoneBlank;
import static junit.framework.TestCase.*;

/**
 * Created by ruslan on 24.12.2016.
 */
public class NoteItemTest {
    private static final int TITLE_DEFAULT_LENGTH = 20;
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
    public void testAddingNoteItemWithoutTitle() {
        User user = new User(1123581321L, "Foo");
        UserStorage storage = getOrCreateStorage(user);
        String expectedTitle = "The quick brown fox jumps over the lazy dog".substring(0, TITLE_DEFAULT_LENGTH);

        Item item = storage.addItem("/", "The quick brown fox jumps over the lazy dog");

        assertNotNull(item);
        assertTrue(item instanceof NoteItem);
        NoteItem note = (NoteItem) item;
        assertEquals(ItemType.NOTES, note.getType());
        assertNoneBlank(note.getName());
        assertTrue(note.getName().endsWith(".note"));
        assertEquals("/notes/" + note.getName(), note.getFullPath());
        assertEquals(expectedTitle, note.getTitle());
        assertEquals("The quick brown fox jumps over the lazy dog", note.getContent());
    }


    @Test
    public void testAddingNoteItemWithTitle() {
        User user = new User(1123581321L, "Foo");
        UserStorage storage = getOrCreateStorage(user);

        Item item = storage.addItem("/", " Brown fox; The quick brown fox jumps over the lazy dog ");

        assertNotNull(item);
        assertTrue(item instanceof NoteItem);
        NoteItem note = (NoteItem) item;
        assertEquals(ItemType.NOTES, note.getType());
        assertNoneBlank(note.getName());
        assertTrue(note.getName().endsWith(".note"));
        assertEquals("/notes/" + note.getName(), note.getFullPath());
        assertEquals("Brown fox", note.getTitle());
        assertEquals("Brown fox; The quick brown fox jumps over the lazy dog", note.getContent());
    }

    @Test
    public void testAddingNoteItemWithLongTitle() {
        User user = new User(1123581321L, "Foo");
        UserStorage storage = getOrCreateStorage(user);

        Item item = storage.addItem("/", " Brown fox! Brown fox! Brown fox!; The quick brown fox jumps over the lazy dog ");

        assertNotNull(item);
        assertTrue(item instanceof NoteItem);
        NoteItem note = (NoteItem) item;
        assertEquals(ItemType.NOTES, note.getType());
        assertNoneBlank(note.getName());
        assertTrue(note.getName().endsWith(".note"));
        assertEquals("/notes/" + note.getName(), note.getFullPath());
        assertEquals("Brown fox! Brown fox", note.getTitle());
        assertEquals("Brown fox! Brown fox! Brown fox!; The quick brown fox jumps over the lazy dog", note.getContent());
    }

    @Test
    public void testAddingNoteItemWithTitle2() {
        User user = new User(1123581321L, "Foo");
        UserStorage storage = getOrCreateStorage(user);

        Item item = storage.addItem("/alerts", "  Brown fox\n The quick brown fox jumps over the lazy dog ");

        assertNotNull(item);
        assertTrue(item instanceof NoteItem);
        NoteItem note = (NoteItem) item;
        assertEquals(ItemType.NOTES, note.getType());
        assertNoneBlank(note.getName());
        assertTrue(note.getName().endsWith(".note"));
        assertEquals("/notes/" + note.getName(), note.getFullPath());
        assertEquals("Brown fox", note.getTitle());
        assertEquals("Brown fox\n The quick brown fox jumps over the lazy dog", note.getContent());
    }

    @Test
    public void testAddingMultipleNoteItems() {
        User user = new User(1123581321L, "Foo");
        UserStorage storage = getOrCreateStorage(user);

        Item item = storage.addItem("/", "  Brown fox; The quick brown fox jumps over the lazy dog");
        assertNotNull(item);
        assertTrue(item instanceof NoteItem);
        item = storage.addItem("/", "  Brown fox\nThis is \n\n\nfull content of the note.\n\n");
        assertNotNull(item);
        assertTrue(item instanceof NoteItem);


        Item notes = storage.getItemByPath(ItemType.NOTES);
        assertNotNull(notes);
        assertEquals(ItemType.NOTES, notes.getType());
        List<NoteItem> allNotes = notes.listItems();

        assertEquals(2, allNotes.size());

        for (NoteItem item1 : allNotes) {
            assertEquals(ItemType.NOTES, item1.getType());
            assertNoneBlank(item1.getName());
            assertTrue(item1.getName().endsWith(".note"));
            assertEquals("/notes/" + item1.getName(), item1.getFullPath());
        }

        assertEquals("Brown fox; The quick brown fox jumps over the lazy dog", allNotes.get(0).getContent());
        assertEquals("Brown fox\nThis is \n\n\nfull content of the note.", allNotes.get(1).getContent());
    }

    private UserStorage getOrCreateStorage(User user) {
        return (FileUserStorage) fileStorage.getOrCreate(user);
    }
}
