package io.arusland.storage.file;

import io.arusland.storage.*;
import io.arusland.storage.util.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.arusland.storage.ItemType.ALERTS;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.stream.Collectors.toList;

/**
 * Created by ruslan on 03.12.2016.
 */
public class FileUserStorage implements UserStorage, ItemFactory {
    protected final Logger log = Logger.getLogger(getClass());
    private final SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
    private final SimpleDateFormat BACKUP_FILE_NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final User user;
    private final File root;
    private List<Item> rootItems;
    private TimeZoneClient timeZoneClient;

    FileUserStorage(User user, File root) {
        this.user = Validate.notNull(user, "user");
        try {
            this.root = Validate.notNull(root, "root").getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException("Root is invalid: " + root, e);
        }
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

        if (alertInfo != null) {
            if (alertInfo.valid) {
                log.info("Parsed alert: " + alertInfo);
                return tryAddAlertItem(path, alertInfo);
            }

            if (!alertInfo.valid && StringUtils.isNotBlank(alertInfo.validMessage)) {
                throw new StorageException(alertInfo.validMessage);
            }
        }

        NoteInfo noteInfo = NoteInfo.parse(content);

        if (noteInfo != null) {
            log.info("Parsed note: " + noteInfo);
            return tryAddNoteItem(path, noteInfo);
        }

        return null;
    }

    private String generateAlertFileName() {
        return FILE_NAME_FORMAT.format(new Date()) + ".alert";
    }

    private String generateNoteFileName() {
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
    public Item moveItem(String pathFrom, String pathTo) {
        FileItem item = getFileItemByPath(pathFrom, false);
        FileItem pathTarget = getFileItemByPath(pathTo, true);
        File targetFile = new File(pathTarget.getFile(), item.getName());
        String targetFilePath = pathTarget.getFullPath() + "/" + item.getName();

        try {
            log.info(String.format("Moving from '%s' to '%s'", item.getFile(), targetFile));
            Files.move(item.getFile().toPath(), targetFile.toPath(), REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return getItemByPath(targetFilePath);
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

    @Override
    public File createBackFile() {
        String name = "_" + BACKUP_FILE_NAME_FORMAT.format(getTimeZoneClient().toClient(new Date()));
        File tempFile = new File(System.getProperty("java.io.tmpdir"), root.getName() + name + ".zip");

        if (tempFile.exists()) {
            tempFile.delete();
        }

        ZipUtil.zipDir(Arrays.asList(root), tempFile);

        return tempFile;
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

        if (file.isDirectory()) {
            return Optional.of(new FileItem(user, itemPath.getType(), file, itemPath, this));
        }

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
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            AlertModel model = AlertModel.parseFile(content);
            AlertInfo info = AlertInfo.parse(model.getInput(), getTimeZoneClient());

            if (info != null && info.valid) {
                return Optional.of(new FileAlertItem(user, file, itemPath, info,
                        model.getLastActivePeriodTimeDate(), (ItemFactory) this,
                        getTimeZoneClient(), this::onPeriodTimeUpdate));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    private void onPeriodTimeUpdate(FileAlertItem fileAlertItem) {
        AlertModel model = new AlertModel(fileAlertItem.getInfo().content, fileAlertItem.getLastActivePeriodTime());

        log.info("Alert updated in file: " + fileAlertItem.getFile());

        try {
            FileUtils.writeStringToFile(fileAlertItem.getFile(), model.toFileString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Saving alert failed: " + fileAlertItem.getFile(), e);
        }
    }

    private FileAlertItem tryAddAlertItem(String path, AlertInfo info) {
        FileItem parent = getFileItemByPath(path, true);

        if (parent == null || parent.getType() != ALERTS || !parent.isDirectory()) {
            parent = (FileItem) getItemByPath(ALERTS);
        }

        if (parent != null) {
            String name = generateAlertFileName();
            File file = new File(parent.getFile(), name);
            AlertModel model = new AlertModel(info.content, (Date) null);

            log.info("Alert save into file: " + file);

            try {
                FileUtils.writeStringToFile(file, model.toFileString(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ItemPath itemPath = ItemPath.parse(parent.getFullPath() + "/" + file.getName());
            return new FileAlertItem(user, file, itemPath, info, (Date) null,
                    (ItemFactory) this, getTimeZoneClient(), this::onPeriodTimeUpdate);
        }

        return null;
    }

    private Item tryAddNoteItem(String path, NoteInfo info) {
        FileItem parent = getFileItemByPath(path, true);

        if (parent == null || parent.getType() != ItemType.NOTES || !parent.isDirectory()) {
            parent = (FileItem) getItemByPath(ItemType.NOTES);
        }

        if (parent != null) {
            List<FileItem> children = parent.listItems();
            Optional<FileNoteItem> withTheSameName = children.stream()
                    .filter(p -> !p.isDirectory() && p.getTitle().equals(info.title))
                    .map(p -> (FileNoteItem) p)
                    .findFirst();

            if (withTheSameName.isPresent()) {
                FileNoteItem fitem = withTheSameName.get();
                File file = fitem.getFile();
                StringBuilder sb = new StringBuilder(fitem.getContent());
                sb.append("\n\n");
                sb.append(info.content);

                NoteInfo infoNew = NoteInfo.parse(sb.toString());

                try {
                    FileUtils.writeStringToFile(file, infoNew.content, "UTF-8");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                log.info("Note updated: " + fitem.getTitle());

                ItemPath itemPath = ItemPath.parse(parent.getFullPath() + "/" + file.getName());
                return new FileNoteItem(user, file, itemPath, infoNew, this);
            } else {
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
