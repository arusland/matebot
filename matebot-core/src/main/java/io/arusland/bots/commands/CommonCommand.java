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
            handleTextMessage(update, msg, user, storage, storage);
        } else {
            sendMessage(msg.getChatId(), "It's not a text!");
        }
    }

    private void handleTextMessage(Update update, Message message, User user, UserStorage userStorage, UserStorage storage) {
        if (message.getText().startsWith(BotCommand.COMMAND_INIT_CHARACTER)) {
            ShortcutCommand cmd = getContext().getShortcutCommand(user, message.getText());

            if (cmd != null) {
                handleShortcutCommand(cmd, user, update, storage);
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

    private void handleShortcutCommand(ShortcutCommand cmd, User user, Update update, UserStorage storage) {
        if (cmd.hasArguments()) {
            if ("cd".equals(cmd.getCommand())) {

                getContext().setCurrentDir(user, cmd.getArguments().get(0));
                getContext().listCurrentDir(update);

            } else if ("dl".equals(cmd.getCommand())) {
                Item item = storage.getItemByPath(cmd.getArguments().get(0));

                if (item != null && !item.isDirectory()) {
                    File file = item.tryGetFile();

                    if (file != null && !file.isDirectory()) {
                        getContext().sendFile(update.getMessage().getChatId(), file);
                    }
                } else {
                    sendMessage(update.getMessage().getChatId(), "Cannot find item: " + cmd.getArguments());
                }
            } else if ("rm".equals(cmd.getCommand())) {
                Item item = storage.getItemByPath(cmd.getArguments().get(0));

                if (item != null && !item.isDirectory()) {
                    storage.deleteItem(item);
                    getContext().listCurrentDir(update);
                }
            } else if ("mv".equals(cmd.getCommand())) {
                // move file
                sendMessage(update.getMessage().getChatId(), "TODO: Moving file: " + cmd.getArguments());
            } else {
                System.out.println("Unknown command: " + cmd.getCommand() + " by shortcut: " + cmd.getShortcut());
            }
        }
    }
}
