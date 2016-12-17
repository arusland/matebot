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
    /**
     * Owner of the file.
     */
    User getUser();

    /**
     * Defined type of Item.
     */
    ItemType getType();

    /**
     * Name of the item with extension.
     */
    String getName();

    /**
     * Title of the item. If <code>title</code> is not empty
     * it should be shown instead of <code>name</code>.
     */
    String getTitle();

    /**
     * Full path of the item.
     */
    String getFullPath();

    /**
     * Full path of parent item.
     */
    String getParentPath();

    /**
     * Modification date of the item.
     */
    Date getModifiedDate();

    /**
     * Item size in bytes.
     */
    long getSize();

    /**
     * Return <code>true</code> when item is container of other items.
     */
    boolean isDirectory();

    /**
     * If item is directory this method returns child items in it.
     */
    List<T> listItems();

    /**
     * Returns file if it can. Otherwise <code>null</code>.
     */
    File tryGetFile();
}
