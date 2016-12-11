package io.arusland.storage.file;

import io.arusland.storage.AlertItem;
import io.arusland.storage.ItemPath;
import io.arusland.storage.ItemType;
import io.arusland.storage.User;

import java.io.File;
import java.util.Date;

/**
 * Created by ruslan on 10.12.2016.
 */
public class FileAlertItem extends FileItem implements AlertItem {
    public FileAlertItem(User user, ItemType type, File file, ItemPath path) {
        super(user, type, file, path);
    }

    @Override
    public Date nextTime() {
        return null;
    }

    @Override
    public String getMessage() {
        return null;
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
