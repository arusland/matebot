package io.arusland.storage;

/**
 * Stores textual data.
 * <p>
 * Title can be defined by ';' or '\n'.
 * <p>
 * Created by ruslan on 24.12.2016.
 */
public interface NoteItem extends Item<NoteItem> {
    /**
     * Content of the note.
     */
    String getContent();
}
