package io.arusland.storage.file;

import io.arusland.storage.ItemPath;
import io.arusland.storage.ItemType;
import io.arusland.storage.Item;
import io.arusland.storage.User;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * File implementation for {@link Item}.
 * <p>
 * Created by ruslan on 03.12.2016.
 */
public class FileItem implements Item, Comparator<Item> {
    private final User user;
    private final ItemType type;
    private final File file;
    private final ItemPath path;

    public FileItem(User user, ItemType type, File file, ItemPath path) {
        this.user = Validate.notNull(user, "user");
        this.type = Validate.notNull(type, "type");
        this.file = Validate.notNull(file, "file");
        this.path = Validate.notNull(path, "path");
    }

    public User getUser() {
        return user;
    }

    public ItemType getType() {
        return type;
    }

    public String getName() {
        return file.getName();
    }

    public String getFullPath() {
        return path.getPath();
    }

    @Override
    public String getParentPath() {
        return path.getParentPath();
    }

    public Date getModifiedDate() {
        return new Date(file.lastModified());
    }

    public long getSize() {
        return file.length();
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public List<Item> listItems() {
        if (isDirectory()) {
            List<Item> result = Arrays.stream(file.listFiles(TypedFileFilter.get(type)))
                    .map(f -> new FileItem(user, type, f,
                            ItemPath.parse(path.getPath() + "/" + f.getName())))
                    .collect(toList());

            result.sort(this);

            return result;
        }

        return Collections.emptyList();
    }

    @Override
    public File tryGetFile() {
        return file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public int compare(Item o1, Item o2) {
        if (o1.isDirectory()) {
            if (o2.isDirectory()) {
                return o1.getName().compareTo(o2.getName());
            }

            return 1;
        } else if (o2.isDirectory()) {
            return -1;
        } else {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
