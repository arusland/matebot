package io.arusland.storage.file;

import io.arusland.storage.Item;
import io.arusland.storage.ItemPath;
import io.arusland.storage.ItemType;

import java.io.File;
import java.util.Calendar;
import java.util.Optional;

/**
 * Created by ruslan on 15.12.2016.
 */
public interface ItemFactory {
    Optional<Item> fromFile(ItemType type, File file, ItemPath itemPath);
}
