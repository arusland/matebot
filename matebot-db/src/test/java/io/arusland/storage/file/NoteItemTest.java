package io.arusland.storage.file;

import io.arusland.storage.*;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static io.arusland.storage.TestUtils.assertNoneBlank;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by ruslan on 24.12.2016.
 */
public class NoteItemTest {
    private static final int TITLE_DEFAULT_LENGTH = 40;
    private Path root;
    private FileStorage fileStorage;

    @BeforeEach
    public void beforeEachTest() throws IOException {
        root = Files.createTempDirectory("matebot");
        fileStorage = new FileStorage(root.toString(), Collections.emptyMap());
    }

    @AfterEach
    public void afterEachTest() throws IOException {
        FileUtils.deleteDirectory(root.toFile());
    }

    @Test
    public void testAddingNoteItemWithoutTitle() {
        User user = new User(1123581321L, "Foo");
        UserStorage storage = getOrCreateStorage(user);
        String expectedTitle = "The quick brown fox jumps over the lazy dog".substring(0, TITLE_DEFAULT_LENGTH).trim();

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
        assertEquals("Brown fox! Brown fox! Brown fox!", note.getTitle());
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
        item = storage.addItem("/", "  Brown fox1\nThis is \n\n\nfull content of the note.\n\n");
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
        assertEquals("Brown fox1\nThis is \n\n\nfull content of the note.", allNotes.get(1).getContent());
    }

    @Test
    public void testUpdatingExistingNoteItemWithTheSameTitle() {
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

        assertEquals(1, allNotes.size());

        for (NoteItem item1 : allNotes) {
            assertEquals(ItemType.NOTES, item1.getType());
            assertNoneBlank(item1.getName());
            assertTrue(item1.getName().endsWith(".note"));
            assertEquals("/notes/" + item1.getName(), item1.getFullPath());
        }

        assertEquals("Brown fox; The quick brown fox jumps over the lazy dog\n" +
                "\n" +
                "Brown fox\n" +
                "This is \n" +
                "\n" +
                "\n" +
                "full content of the note.", allNotes.get(0).getContent());
    }

    private UserStorage getOrCreateStorage(User user) {
        return (FileUserStorage) fileStorage.getOrCreate(user);
    }
}
