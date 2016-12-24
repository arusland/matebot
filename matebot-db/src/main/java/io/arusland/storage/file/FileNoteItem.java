package io.arusland.storage.file;

import io.arusland.storage.*;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by ruslan on 24.12.2016.
 */
public class FileNoteItem extends FileItem<NoteItem> implements NoteItem {
    private final NoteInfo info;

    public FileNoteItem(User user, File file, ItemPath path, NoteInfo info, ItemFactory itemFactory) {
        super(user, ItemType.NOTES, file, path, itemFactory);
        this.info = Validate.notNull(info, "info");
    }

    @Override
    public String getContent() {
        return info.content;
    }

    @Override
    public String getTitle() {
        return info.title;
    }

    @Override
    public List<NoteItem> listItems() {
        if (isDirectory()) {
            List<NoteItem> result = Arrays.stream(getFile()
                    .listFiles(TypedFileFilter.get(ItemType.NOTES)))
                    .map(f -> getItemFactory().fromFile(ItemType.NOTES, f, ItemPath.parse(getFullPath() + "/" + f.getName())))
                    .filter(p -> p.isPresent())
                    .map(p -> (NoteItem) p.get())
                    .collect(toList());

            result.sort(this);

            return result;
        }

        return Collections.emptyList();
    }
}
