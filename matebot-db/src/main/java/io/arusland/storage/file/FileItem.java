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
public class FileItem<T extends Item> implements Item<T>, Comparator<Item> {
    private final User user;
    private final ItemType type;
    private final File file;
    private final ItemPath path;
    private final ItemFactory itemFactory;

    public FileItem(User user, ItemType type, File file, ItemPath path, ItemFactory itemFactory) {
        this.user = Validate.notNull(user, "user");
        this.type = Validate.notNull(type, "type");
        this.file = Validate.notNull(file, "file");
        this.path = Validate.notNull(path, "path");
        this.itemFactory = Validate.notNull(itemFactory, "itemFactory");
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

    @Override
    public String getTitle() {
        return "";
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
        if (file.isFile()) {
            return file.length();
        }

        return 0L;
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public List<T> listItems() {
        if (isDirectory()) {
            List<Item> result = Arrays.stream(file.listFiles(TypedFileFilter.get(type)))
                    .map(f -> itemFactory.fromFile(type, f,
                            ItemPath.parse(path.getPath() + "/" + f.getName())))
                    .filter(p -> p.isPresent())
                    .map(p -> p.get())
                    .collect(toList());

            result.sort(this);

            return (List<T>) result;
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

    protected ItemFactory getItemFactory() {
        return itemFactory;
    }

    @Override
    public String toString() {
        return "FileItem{" +
                "user=" + user +
                ", type=" + type +
                ", file=" + file +
                ", path=" + path +
                '}';
    }
}
