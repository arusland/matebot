package io.arusland.storage;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Stored item.
 * <p>
 * Created by ruslan on 03.12.2016.
 */
public interface Item<T extends Item> {
    User getUser();

    ItemType getType();

    String getName();

    String getFullPath();

    String getParentPath();

    Date getModifiedDate();

    long getSize();

    boolean isDirectory();

    List<T> listItems();

    File tryGetFile();
}
