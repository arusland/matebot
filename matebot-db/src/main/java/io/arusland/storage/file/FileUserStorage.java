package io.arusland.storage.file;

import io.arusland.storage.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.arusland.storage.ItemType.ALERTS;
import static java.util.stream.Collectors.toList;

/**
 * Created by ruslan on 03.12.2016.
 */
public class FileUserStorage implements UserStorage, ItemFactory {
    protected final Logger log = Logger.getLogger(getClass());
    private final static SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
    private final User user;
    private final File root;
    private List<Item> rootItems;
    private TimeZoneClient timeZoneClient;

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
    public Item addItem(String path, String content) {
        AlertInfo alertInfo = AlertInfo.parse(content, getTimeZoneClient());

        if (alertInfo != null && alertInfo.valid) {
            log.info("Parsed alert: " + alertInfo);
            return tryAddAlertItem(path, alertInfo);
        }

        NoteInfo noteInfo = NoteInfo.parse(content);

        if (noteInfo != null) {
            log.info("Parsed note: " + noteInfo);
            return tryAddNoteItem(path, noteInfo);
        }

        return null;
    }

    private static String generateAlertFileName() {
        return FILE_NAME_FORMAT.format(new Date()) + ".alert";
    }

    private static String generateNoteFileName() {
        return FILE_NAME_FORMAT.format(new Date()) + ".note";
    }

    @Override
    public Item addItem(String path, String name, File file) {
        FileItem parent = getFileItemByPath(path, true);

        if (parent == null) {
            return addItem(name, file);
        }

        return addItemIntoParentItem(name, file, parent);
    }

    @Override
    public Item addItem(String name, File content) {
        ItemType type = ItemType.fromFileName(name);
        Item parent = getItemByPath(type);

        return addItemIntoParentItem(name, content, (FileItem) parent);
    }

    @Override
    public void setTimeZone(TimeZone timeZone) {
        this.timeZoneClient = timeZone != null ? TimeZoneClientStandard.create(timeZone) : null;
    }

    @Override
    public TimeZone getTimeZone() {
        return getTimeZoneClient().getTimeZone();
    }

    private TimeZoneClient getTimeZoneClient() {
        if (timeZoneClient == null) {
            timeZoneClient = TimeZoneClientStandard.create(TimeZone.getDefault());
        }

        return timeZoneClient;
    }

    @Override
    public Optional<Item> fromFile(ItemType type, File file, ItemPath itemPath) {
        ItemType fileType = ItemType.fromFileName(file.getName());

        if (fileType == type) {
            switch (fileType) {
                case ALERTS:
                    Optional<FileAlertItem> alert = createAlertItem(file, itemPath);

                    if (alert.isPresent()) {
                        return Optional.of(alert.get());
                    }
                    break;
                case NOTES:
                    Optional<FileNoteItem> note = createNoteItem(file, itemPath);

                    if (note.isPresent()) {
                        return Optional.of(note.get());
                    }
                    break;
                default:
                    return Optional.of(new FileItem(user, itemPath.getType(), file, itemPath, this));
            }
        }

        return Optional.empty();
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
            return new FileItem(user, parent.getType(), file, itemPath, this);
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

            File targetFile = new File(root, itemPath.getPath());

            if (createDirsIf && !targetFile.exists()) {
                targetFile.mkdirs();
            }

            if (targetFile.exists()) {
                if (targetFile.isDirectory()) {
                    return new FileItem(user, itemPath.getType(), targetFile, itemPath, this);
                } else {
                    Optional<Item> item = fromFile(itemPath.getType(), targetFile, itemPath);

                    if (item.isPresent()) {
                        return (FileItem) item.get();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Creates {@link FileAlertItem} from file.
     */
    private Optional<FileAlertItem> createAlertItem(File file, ItemPath itemPath) {
        try {
            String content = FileUtils.readFileToString(file, "UTF-8");
            AlertInfo info = AlertInfo.parse(content, getTimeZoneClient());

            if (info != null) {
                return Optional.of(new FileAlertItem(user, file, itemPath, info, this, getTimeZoneClient()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    private FileAlertItem tryAddAlertItem(String path, AlertInfo info) {
        FileItem parent = getFileItemByPath(path, true);

        if (parent == null || parent.getType() != ALERTS || !parent.isDirectory()) {
            parent = (FileItem) getItemByPath(ALERTS);
        }

        if (parent != null) {
            String name = generateAlertFileName();
            File file = new File(parent.getFile(), name);

            try {
                FileUtils.writeStringToFile(file, info.content, "UTF-8");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ItemPath itemPath = ItemPath.parse(parent.getFullPath() + "/" + file.getName());
            return new FileAlertItem(user, file, itemPath, info, this, getTimeZoneClient());
        }

        return null;
    }

    private Item tryAddNoteItem(String path, NoteInfo info) {
        FileItem parent = getFileItemByPath(path, true);

        if (parent == null || parent.getType() != ItemType.NOTES || !parent.isDirectory()) {
            parent = (FileItem) getItemByPath(ItemType.NOTES);
        }

        if (parent != null) {
            String name = generateNoteFileName();
            File file = new File(parent.getFile(), name);

            try {
                FileUtils.writeStringToFile(file, info.content, "UTF-8");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ItemPath itemPath = ItemPath.parse(parent.getFullPath() + "/" + file.getName());
            return new FileNoteItem(user, file, itemPath, info, this);
        }

        return null;
    }

    /**
     * Create {@link FileNoteItem} from file.
     */
    private Optional<FileNoteItem> createNoteItem(File file, ItemPath itemPath) {
        try {
            String content = FileUtils.readFileToString(file, "UTF-8");
            NoteInfo info = NoteInfo.parse(content);

            if (info != null) {
                return Optional.of(new FileNoteItem(user, file, itemPath, info, this));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }
}
