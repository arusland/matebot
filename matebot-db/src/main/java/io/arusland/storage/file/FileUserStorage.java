package io.arusland.storage.file;

import io.arusland.storage.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by ruslan on 03.12.2016.
 */
public class FileUserStorage implements UserStorage {
    private final User user;
    private final File root;
    private List<Item> rootItems;

    FileUserStorage(User user, File root) {
        this.user = Validate.notNull(user, "user");
        this.root = Validate.notNull(root, "root");
    }

    public User getUser() {
        return user;
    }

    public Item getItemByPath(ItemType type) {
        if (type == ItemType.ROOT) {
            return null;
        }

        return getItemByPath(type.normalized());
    }

    public Item getItemByPath(String path) {
        return getFileItemByPath(path, false);
    }

    public List<Item> listItems(String path) {
        ItemPath itemPath = ItemPath.parse(path);

        if (itemPath.isRoot()) {
            return getRootItems();
        }

        FileItem item = getFileItemByPath(path, false);

        if (item != null) {
            return item.listItems();
        }

        return Collections.emptyList();
    }

    @Override
    public List<Item> listItems(ItemType type) {
        return listItems(type.normalized());
    }

    public void deleteItem(Item item) {
        FileItem fileItem = (FileItem) item;

        if (fileItem != null) {
            fileItem.getFile().delete();
        }
    }

    @Override
    public Item addItem(String path, String name, String content) {



        FileItem parent = getFileItemByPath(path, true);

        if (parent != null) {
            File file = new File(parent.getFile(), name);

            try {
                FileUtils.writeStringToFile(file, content, "UTF-8");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ItemPath itemPath = ItemPath.parse(parent.getFullPath() + "/" + file.getName());
            return new FileItem(user, parent.getType(), file, itemPath);
        }

        return null;
    }

    @Override
    public Item addItem(String path, String name, File content) {
        FileItem parent = getFileItemByPath(path, true);

        if (parent == null) {
            return addItem(name, content);
        }

        return addItemIntoParentItem(name, content, parent);
    }

    @Override
    public Item addItem(String name, File content) {
        ItemType type = ItemType.fromFileName(name);
        Item parent = getItemByPath(type);

        return addItemIntoParentItem(name, content, (FileItem) parent);
    }

    private Item addItemIntoParentItem(String name, File content, FileItem parent) {
        if (parent != null) {
            if (!parent.isDirectory()) {
                throw new StorageException("Cannot write file into file: " + parent.getFullPath());
            }

            ItemType type = ItemType.fromFileName(name);

            // suggested path is wrong type
            // put file into right place
            if (parent.getType() != type) {
                return addItem(name, content);
            }

            File file = new File(parent.getFile(), name);
            String futurePath = parent.getFullPath() + "/" + file.getName();

            try {
                Files.copy(content.toPath(), file.toPath());
            } catch (FileAlreadyExistsException e) {
                throw new StorageException("File already exists: " + futurePath, e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ItemPath itemPath = ItemPath.parse(futurePath);
            return new FileItem(user, parent.getType(), file, itemPath);
        }

        return null;
    }

    private List<Item> getRootItems() {
        if (rootItems == null) {
            List<Item> result = Arrays.stream(ItemType.values())
                    .filter(p -> p != ItemType.ROOT)
                    .map(i -> getFileItemByPath(i.normalized(), false))
                    .filter(p -> p != null)
                    .sorted(Comparator.comparing(FileItem::getName))
                    .collect(toList());

            rootItems = Collections.unmodifiableList(result);
        }

        return rootItems;
    }

    private FileItem getFileItemByPath(String path, boolean createDirsIf) {
        ItemPath itemPath = ItemPath.parse(path);

        if (!itemPath.isRoot()) {
            File typeDir = new File(root, itemPath.getType().normalized());

            if (!typeDir.exists()) {
                typeDir.mkdirs();
            }

            File result = new File(root, itemPath.getPath());

            if (createDirsIf && !result.exists()) {
                result.mkdirs();
            }

            if (result.exists()) {
                return new FileItem(user, itemPath.getType(), result, itemPath);
            }
        }

        return null;
    }
}
