package io.arusland.bots.commands;

import io.arusland.bots.base.BaseBotCommand;
import io.arusland.bots.base.BotContext;
import io.arusland.bots.utils.TimeUtils;
import io.arusland.storage.AlertItem;
import io.arusland.storage.Item;
import io.arusland.storage.NoteItem;
import io.arusland.storage.UserStorage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.message.internal.StringBuilderUtils;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.File;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handle shortcut commands, file operations.
 * <p>
 * Created by ruslan on 03.12.2016.
 */
public class CommonCommand extends BaseBotCommand {
    private final static String COMMAND_CANCEL = "/cancel";
    private final static DateFormat NAME_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private final static DateFormat DATETIME_FORMAT = new SimpleDateFormat("HH:mm dd.MM.yyyy");
    private boolean creatingNewDirectory;
    private Item moveFileFrom;

    public CommonCommand(BotContext context) {
        super("common", "This command not visible in menu!", context);
    }

    @Override
    public void execute(AbsSender absSender, Update update, String[] arguments) {
        User user = update.getMessage().getFrom();
        UserStorage storage = getContext().getUserStorage(user);
        Message msg = update.getMessage();

        if (msg.hasText()) {
            handleTextMessage(update, msg, user, storage);
        } else {
            handleFileUpload(update, msg, user, storage);
        }
    }

    private void handleFileUpload(Update update, Message message, User user, UserStorage storage) {
        String fileId = "";
        String fileName = "";
        String altExt = "";

        if (message.getDocument() != null) {
            fileId = message.getDocument().getFileId();
            fileName = message.getDocument().getFileName();
        } else if (message.getAudio() != null) {
            fileId = message.getAudio().getFileId();
            fileName = message.getAudio().getTitle();
            altExt = ".mp3";
        } else if (message.getVideo() != null) {
            fileId = message.getVideo().getFileId();
            fileName = getNewFileName();
            altExt = ".mp4";
        } else if (message.getVoice() != null) {
            fileId = message.getVoice().getFileId();
            fileName = getNewFileName();
            altExt = ".ogg";
        } else if (message.getPhoto() != null && !message.getPhoto().isEmpty()) {
            fileId = message.getPhoto().get(message.getPhoto().size() - 1).getFileId();
            fileName = getNewFileName();
            altExt = ".jpg";
        }

        if (StringUtils.isNotBlank(fileId)) {
            GetFile getFile = new GetFile();
            getFile.setFileId(fileId);
            File file = getContext().doGetFile(getFile);

            if (StringUtils.isNoneBlank(file.getFilePath())) {
                String ext = FilenameUtils.getExtension(file.getFilePath());

                if (!FilenameUtils.getExtension(fileName).equalsIgnoreCase(ext)) {
                    fileName += "." + ext;
                }
            } else {
                fileName += altExt;
            }

            java.io.File downloadedFile = getContext().doDownloadFile(file);

            try {
                String currentPath = getCurrentPath(user);
                Item addedItem = storage.addItem(currentPath, fileName, downloadedFile);

                if (addedItem != null) {
                    String moveFileShortcut = "/move";
                    getContext().setCurrentDir(user, addedItem.getParentPath());
                    StringBuilder sb = new StringBuilder("Item '");
                    sb.append(addedItem.getFullPath());
                    sb.append("' added!\n");
                    sb.append("➡️");
                    sb.append(moveFileShortcut);
                    sendMessage(message.getChatId(), sb.toString());

                    getContext().addShortcutCommand(user.getId(), moveFileShortcut, "mv", addedItem.getFullPath());
                } else {
                    sendMessage(message.getChatId(), "⚠ File adding failed!");
                }
            } finally {
                downloadedFile.delete();
            }
        }
    }

    private void handleTextMessage(Update update, Message message, User user, UserStorage storage) {
        if (message.getText().startsWith(BotCommand.COMMAND_INIT_CHARACTER)) {
            ShortcutCommand cmd = getContext().getShortcutCommand(user.getId(), message.getText());

            if (cmd != null) {
                getContext().clearShortcutCommands(user);
                handleShortcutCommand(cmd, user, update.getMessage().getChatId(), update, storage);
                return;
            } else if (COMMAND_CANCEL.equals(message.getText())) {
                moveFileFrom = null;
                creatingNewDirectory = false;
                getContext().clearShortcutCommands(user);
                sendMessage(message.getChatId(), "Operation canceled");
                return;
            } else {
                sendMessage(message.getChatId(), "Unknown command!");
                return;
            }
        }

        getContext().clearShortcutCommands(user);

        String msg = message.getText().trim();

        if (StringUtils.isNoneBlank(msg)) {
            if (creatingNewDirectory && moveFileFrom != null) {
                creatingNewDirectory = false;
                moveFileFrom = null;
                Item targetRoot = storage.getItemByPath(moveFileFrom.getType());

                if (targetRoot != null) {
                    String targetPath = targetRoot.getFullPath() + "/" + msg;
                    storage.moveItem(moveFileFrom.getFullPath(), targetPath);
                    sendMessage(message.getChatId(), String.format("Item moved to '%s'", targetPath));
                }
            } else {
                handleTextMessageInternal(message, user, storage, msg);
            }
        }
    }

    private void handleTextMessageInternal(Message message, User user, UserStorage storage, String msg) {
        String currentPath = getCurrentPath(user);
        Item addedItem = storage.addItem(currentPath, msg);

        if (addedItem != null) {
            if (addedItem instanceof AlertItem) {
                handleAlertItem(message.getChatId(), (AlertItem) addedItem, storage, user);
            } else if (addedItem instanceof NoteItem) {
                NoteItem noteItem = (NoteItem) addedItem;
                boolean noteUpdated = msg.length() < noteItem.getContent().length();
                handleNoteItem(message.getChatId(), noteItem, noteUpdated, user.getId());
            } else {
                sendMessage(message.getChatId(), "⚠ unsupported item: " + addedItem);
            }
        } else {
            sendMessage(message.getChatId(), "⚠ invalid input");
        }
    }

    private void handleNoteItem(Long chatId, NoteItem note, boolean noteUpdated, Integer userId) {
        StringBuilder sb = new StringBuilder();
        if (noteUpdated) {
            sb.append("✅ Note updated!");
        } else {
            sb.append("✅ Note added!");
        }

        sb.append(" to ");
        sb.append(note.getParentPath());
        sb.append("\nTitle: " + note.getTitle());
        sb.append("\n➡️/move");

        getContext().addShortcutCommand(userId, "/move", "mv", note.getFullPath());

        sendMessage(chatId, sb.toString());
    }

    private void handleAlertItem(Long chatId, AlertItem addedItem, UserStorage storage, User user) {
        Date nextTime = addedItem.nextTime();
        long diff = nextTime.getTime() - System.currentTimeMillis();

        if (diff > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("✅ Alert added!\n");
            sb.append("Notification time: ");
            sb.append(DATETIME_FORMAT.format(getContext().toClient(user, nextTime)));
            sb.append("\n");
            sb.append("\uD83D\uDD14 Notification in ");
            sb.append(TimeUtils.friendlyTimespan(nextTime));

            sendMessage(chatId, sb.toString());
            getContext().rerunAlerts();

            return;
        }

        log.warn("Item removing " + addedItem + "; next time is " + nextTime);
        storage.deleteItem(addedItem);
    }

    private void handleShortcutCommand(ShortcutCommand cmd, User user, Long chatId, Update update, UserStorage storage) {
        if (cmd.hasArguments()) {
            List<String> args = cmd.getArguments();
            String command = cmd.getCommand();
            if ("cd".equals(command)) {
                getContext().setCurrentDir(user, args.get(0));
                getContext().listCurrentDir(update);

            } else if ("dl".equals(command)) {
                Item item = storage.getItemByPath(args.get(0));

                if (item != null) {
                    if (item instanceof AlertItem) {
                        handleDownloadingAlertItem(chatId, (AlertItem) item, user);
                    } else if (item instanceof NoteItem) {
                        handleDownloadingNoteItem(chatId, (NoteItem) item, user);
                    } else {
                        handleDownloadingCommonItem(chatId, item);
                    }
                } else {
                    sendMessage(chatId, "File not found!");
                }
            } else if ("rm".equals(command)) {
                Item item = storage.getItemByPath(args.get(0));

                if (item != null) {
                    if (item instanceof AlertItem) {
                        handleRemovingAlertItem(cmd, user, chatId, update, storage, (AlertItem) item);
                    } else if (item instanceof NoteItem) {
                        handleRemovingNoteItem(cmd, user, chatId, update, storage, (NoteItem) item);
                    } else {
                        handleRemovingCommonItem(cmd, user, chatId, update, storage, item);
                    }
                } else {
                    sendMessage(chatId, "File not found!");
                }
            } else if ("mv".equals(command)) {
                Item item = storage.getItemByPath(args.get(0));

                if (item != null) {
                    if (!(item instanceof AlertItem)) {
                        if (args.size() > 1) {
                            String itemFrom = args.get(0);
                            String targetPath = args.get(1);
                            storage.moveItem(itemFrom, targetPath);
                            sendMessage(chatId, String.format("Item moved to '%s'", targetPath));
                        } else {
                            String newDir = "/new";
                            List<Item> rootItems = storage.listItems(item.getType())
                                    .stream()
                                    .filter(p -> p.isDirectory())
                                    .collect(Collectors.toList());
                            StringBuilder sb = new StringBuilder();

                            sb.append("Choose directory move to or /cancel\n");

                            for (Item subItem : rootItems) {
                                String cmdText = "/" + subItem.getName();
                                sb.append(cmdText);
                                sb.append("\n");

                                getContext().addShortcutCommand(user.getId(), cmdText, "mv",
                                        item.getFullPath(), subItem.getFullPath());
                            }
                            sb.append(newDir);
                            sendMessage(chatId, sb.toString());

                            getContext().addShortcutCommand(user.getId(), newDir, "newdir", item.getFullPath());
                        }
                    }
                } else {
                    sendMessage(chatId, "File not found!");
                }
            } else if ("newdir".equals(command)) {
                sendMessage(chatId, "Enter new directory name or /cancel");
                creatingNewDirectory = true;
                moveFileFrom = storage.getItemByPath(args.get(0));
            } else {
                log.warn("Unknown command: " + cmd.getCommand() + " by shortcut: " + cmd.getShortcut());
            }
        }
    }

    private void handleRemovingNoteItem(ShortcutCommand cmd, User user, Long chatId, Update update, UserStorage storage, NoteItem note) {
        if (!note.isDirectory()) {
            if (cmd.getArguments().size() >= 2 && cmd.getArguments().get(1).equals("1")) {
                storage.deleteItem(note);
                sendMessage(chatId, "Note was removed");
                getContext().clearShortcutCommands(user);
                getContext().listCurrentDir(update);
            } else {
                String removeFile = "/remove";
                String cancelOperation = "/cancel";

                getContext().addShortcutCommand(user.getId(), removeFile, "rm", note.getFullPath(), "1");
                getContext().addShortcutCommand(user.getId(), cancelOperation, "cd", note.getParentPath());

                StringBuilder sb = new StringBuilder("Are you sure to remove note '");
                sb.append(note.getContent());
                sb.append("'?\n❌");
                sb.append(removeFile);
                sb.append(" ✅");
                sb.append(cancelOperation);
                sb.append("\n");

                sendMessage(chatId, sb.toString());
            }
        }
    }

    private void handleDownloadingNoteItem(Long chatId, NoteItem note, User user) {
        StringBuilder sb = new StringBuilder();

        sb.append("\uD83D\uDCDD Note\n");
        sb.append("Added: ");
        sb.append(DATETIME_FORMAT.format(getContext().toClient(user, note.getModifiedDate())));
        sb.append("\n");
        sb.append(note.getContent());

        String removeFile = "/remove";
        String cancelOperation = "/cancel";
        String moveOperation = "/move";

        getContext().addShortcutCommand(user.getId(), removeFile, "rm", note.getFullPath(), "0");
        getContext().addShortcutCommand(user.getId(), cancelOperation, "cd", note.getParentPath());
        getContext().addShortcutCommand(user.getId(), moveOperation, "mv", note.getFullPath());

        sb.append("\n\n❌");
        sb.append(removeFile);
        sb.append(" ✅");
        sb.append(cancelOperation);
        sb.append(" ➡️");
        sb.append(moveOperation);
        sb.append("\n");

        sendMessage(chatId, sb.toString());
    }

    private void handleDownloadingAlertItem(Long chatId, AlertItem alert, User user) {
        Date nextTime = alert.nextTime();
        StringBuilder sb = new StringBuilder();

        sb.append("✅ Alert");
        if (StringUtils.isNoneBlank(alert.getMessage())) {
            sb.append(": ");
            sb.append(alert.getMessage());
        }
        sb.append("\n");
        sb.append("Notification time: ");
        sb.append(DATETIME_FORMAT.format(getContext().toClient(user, nextTime)));
        sb.append("\n");
        sb.append("Alert source: ");
        sb.append(alert.getSource());
        sb.append("\n");

        if (alert.isActive()) {
            sb.append("\uD83D\uDD14 Notification in ");
            sb.append(TimeUtils.friendlyTimespan(nextTime));
        } else {
            sb.append("❌ Not active");
        }

        String removeFile = "/remove";
        String cancelOperation = "/cancel";

        getContext().addShortcutCommand(user.getId(), removeFile, "rm", alert.getFullPath(), "0");
        getContext().addShortcutCommand(user.getId(), cancelOperation, "cd", alert.getParentPath());

        sb.append("\n\n❌");
        sb.append(removeFile);
        sb.append(" ✅");
        sb.append(cancelOperation);
        sb.append("\n");

        sendMessage(chatId, sb.toString());
    }

    private void handleDownloadingCommonItem(Long chatId, Item item) {
        if (!item.isDirectory()) {
            java.io.File file = item.tryGetFile();

            if (file != null && !file.isDirectory()) {
                getContext().sendFile(chatId, file);
            }
        }
    }

    private void handleRemovingAlertItem(ShortcutCommand cmd, User user, Long chatId, Update update, UserStorage storage, AlertItem alert) {
        if (!alert.isDirectory()) {
            if (cmd.getArguments().size() >= 2 && cmd.getArguments().get(1).equals("1")) {
                storage.deleteItem(alert);
                sendMessage(chatId, "Alert was removed");
                getContext().clearShortcutCommands(user);
                getContext().listCurrentDir(update);
            } else {
                String removeFile = "/remove";
                String cancelOperation = "/cancel";

                getContext().addShortcutCommand(user.getId(), removeFile, "rm", alert.getFullPath(), "1");
                getContext().addShortcutCommand(user.getId(), cancelOperation, "cd", alert.getParentPath());

                StringBuilder sb = new StringBuilder("Are you sure to remove alert '");
                sb.append(alert.getSource());
                sb.append("'?\n❌");
                sb.append(removeFile);
                sb.append(" ✅");
                sb.append(cancelOperation);
                sb.append("\n");

                sendMessage(chatId, sb.toString());
            }
        }
    }

    private void handleRemovingCommonItem(ShortcutCommand cmd, User user, Long chatId, Update update, UserStorage storage, Item item) {
        if (!item.isDirectory()) {
            if (cmd.getArguments().size() >= 2 && cmd.getArguments().get(1).equals("1")) {
                storage.deleteItem(item);
                sendMessage(chatId, "item '" + item.getFullPath() + "' removed");
                getContext().clearShortcutCommands(user);
                getContext().listCurrentDir(update);
            } else {
                String removeFile = "/remove";
                String cancelOperation = "/cancel";

                getContext().addShortcutCommand(user.getId(), removeFile, "rm", item.getFullPath(), "1");
                getContext().addShortcutCommand(user.getId(), cancelOperation, "cd", item.getParentPath());

                StringBuilder sb = new StringBuilder("Are you sure to remove item '");
                sb.append(item.getFullPath());
                sb.append("'?\n❌");
                sb.append(removeFile);
                sb.append(" ✅");
                sb.append(cancelOperation);
                sb.append("\n");

                sendMessage(chatId, sb.toString());
            }
        }
    }

    private static String getNewFileName() {
        return NAME_FORMAT.format(new Date());
    }
}
