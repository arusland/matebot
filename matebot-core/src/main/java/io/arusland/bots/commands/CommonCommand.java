package io.arusland.bots.commands;

import io.arusland.bots.base.BaseBotCommand;
import io.arusland.bots.base.BotContext;
import io.arusland.storage.AlertItem;
import io.arusland.storage.Item;
import io.arusland.storage.UserStorage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.objects.File;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

/**
 * Handle shortcut commands, file operations.
 * <p>
 * Created by ruslan on 03.12.2016.
 */
public class CommonCommand extends BaseBotCommand {
    private final static DateFormat NAME_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private final static DateFormat ALERT_FORMAT = new SimpleDateFormat("HH:mm: dd.MM.yyyy");

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
                    sendMessage(message.getChatId(), "Item '" + addedItem.getFullPath() + "' added!");
                    getContext().setCurrentDir(user, addedItem.getParentPath());
                    getContext().listCurrentDir(update);
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
            ShortcutCommand cmd = getContext().getShortcutCommand(user, message.getText());

            if (cmd != null) {
                handleShortcutCommand(cmd, user, update.getMessage().getChatId(), update, storage);
                return;
            }
        }

        String msg = message.getText().trim();

        if (StringUtils.isNoneBlank(msg)) {
            String currentPath = getCurrentPath(user);
            Item addedItem = storage.addItem(currentPath, msg);

            if (addedItem != null) {
                if (addedItem instanceof AlertItem) {
                    handleAlertItem(message.getChatId(), (AlertItem) addedItem, storage, user);
                } else {
                    // TODO: !!!
                }
            } else {
                sendMessage(message.getChatId(), "⚠ invalid input");
            }
        }
    }

    private void handleAlertItem(Long chatId, AlertItem addedItem, UserStorage storage, User user) {
        long diff = addedItem.nextTime().getTime() - System.currentTimeMillis();

        if (diff > 0) {
            Duration duration = Duration.ofMillis(diff);

            StringBuilder sb = new StringBuilder();
            sb.append("Alert added!\n");
            sb.append("Notification time: ");
            sb.append(ALERT_FORMAT.format(addedItem.nextTime()));
            sb.append("\n");
            sb.append("Notification in ");
            sb.append(duration.toMinutes() + " min.");

            sendMessage(chatId, sb.toString());
            getContext().rerunAlerts(user, chatId);

            return;
        }

        storage.deleteItem(addedItem);
    }

    private void handleShortcutCommand(ShortcutCommand cmd, User user, Long chatId, Update update, UserStorage storage) {
        if (cmd.hasArguments()) {
            if ("cd".equals(cmd.getCommand())) {
                getContext().setCurrentDir(user, cmd.getArguments().get(0));
                getContext().listCurrentDir(update);

            } else if ("dl".equals(cmd.getCommand())) {
                Item item = storage.getItemByPath(cmd.getArguments().get(0));

                if (item != null && !item.isDirectory()) {
                    java.io.File file = item.tryGetFile();

                    if (file != null && !file.isDirectory()) {
                        getContext().sendFile(chatId, file);
                    }
                } else {
                    sendMessage(chatId, "Cannot find item: " + cmd.getArguments());
                }
            } else if ("rm".equals(cmd.getCommand())) {
                Item item = storage.getItemByPath(cmd.getArguments().get(0));

                if (item != null && !item.isDirectory()) {
                    if (cmd.getArguments().size() >= 2 && cmd.getArguments().get(1).equals("1")) {
                        storage.deleteItem(item);
                        sendMessage(chatId, "File '" + item.getFullPath() + "' removed");
                        getContext().clearShortcutCommands(user);
                        getContext().listCurrentDir(update);
                    } else {
                        String removeFile = "/remove";
                        String cancelOperation = "/cancel";

                        getContext().clearShortcutCommands(user);
                        getContext().addShortcutCommand(user, removeFile, "rm", item.getFullPath(), "1");
                        getContext().addShortcutCommand(user, cancelOperation, "cd", item.getParentPath());

                        StringBuilder sb = new StringBuilder("Are you sure to remove file '");
                        sb.append(item.getFullPath());
                        sb.append("'?\n❌");
                        sb.append(removeFile);
                        sb.append(" ✅");
                        sb.append(cancelOperation);
                        sb.append("\n");

                        sendMessage(chatId, sb.toString());
                    }
                }
            } else if ("mv".equals(cmd.getCommand())) {
                // move file
                sendMessage(chatId, "TODO: Moving file: " + cmd.getArguments());
            } else {
                log.warn("Unknown command: " + cmd.getCommand() + " by shortcut: " + cmd.getShortcut());
            }
        }
    }

    private static String getNewFileName() {
        return NAME_FORMAT.format(new Date());
    }
}
