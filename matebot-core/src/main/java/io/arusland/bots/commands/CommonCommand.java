package io.arusland.bots.commands;

import io.arusland.bots.base.BaseBotCommand;
import io.arusland.bots.base.BotContext;
import io.arusland.storage.Item;
import io.arusland.storage.UserStorage;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;

import java.io.File;

/**
 * Handle shortcut commands, file operations.
 * <p>
 * Created by ruslan on 03.12.2016.
 */
public class CommonCommand extends BaseBotCommand {
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
            sendMessage(msg.getChatId(), "It's not a text!");
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

        String currentPath = getContext().getCurrentPath(user);

        if (StringUtils.isNoneBlank(currentPath)) {
            Item currentItem = storage.getItemByPath(getContext().getCurrentPath(user));

            if (currentItem != null) {
                StringBuilder sb = new StringBuilder();

                sb.append("Current dir: ");
                sb.append(currentItem.getFullPath());
                sb.append("\n");

                sendMessage(message.getChat().getId(), sb.toString());
            }
        }
    }

    private void handleShortcutCommand(ShortcutCommand cmd, User user, Long chatId, Update update, UserStorage storage) {
        if (cmd.hasArguments()) {
            if ("cd".equals(cmd.getCommand())) {
                getContext().setCurrentDir(user, cmd.getArguments().get(0));
                getContext().listCurrentDir(update);

            } else if ("dl".equals(cmd.getCommand())) {
                Item item = storage.getItemByPath(cmd.getArguments().get(0));

                if (item != null && !item.isDirectory()) {
                    File file = item.tryGetFile();

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
                System.out.println("Unknown command: " + cmd.getCommand() + " by shortcut: " + cmd.getShortcut());
            }
        }
    }
}
