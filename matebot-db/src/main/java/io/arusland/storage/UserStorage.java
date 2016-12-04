package io.arusland.storage;

import java.io.File;
import java.util.List;

/**
 * Heterogeneous storage associated with certain {@link User}.
 * <p>
 * Created by ruslan on 03.12.2016.
 */
public interface UserStorage {
    User getUser();

    Item getItemByPath(ItemType type);

    Item getItemByPath(String path);

    List<Item> listItems(String path);

    List<Item> listItems(ItemType type);

    void deleteItem(Item item);

    Item addItem(String path, String name, String content);

    Item addItem(String path, String name, File content);
}
