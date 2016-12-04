package io.arusland.storage.file;

import io.arusland.storage.ItemType;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by ruslan on 03.12.2016.
 */
public class TypedFileFilter implements FileFilter {
    private final ItemType type;

    public TypedFileFilter(ItemType type) {
        this.type = Validate.notNull(type, "type");
    }

    @Override
    public boolean accept(File file) {
        return file.isDirectory() ||
                type.extensions().isEmpty() ||
                type.extensions().contains(FilenameUtils.getExtension(file.getName()).toLowerCase());
    }

    public String getDefaultExtension() {
        return type.extensions().get(0);
    }

    public static TypedFileFilter get(ItemType type) {
        return new TypedFileFilter(type);
    }
}
