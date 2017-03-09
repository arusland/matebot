package io.arusland.bots.commands;

import io.arusland.bots.base.BaseBotCommand;
import io.arusland.bots.base.BotContext;
import io.arusland.storage.UserStorage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import java.io.File;

/**
 * @author Ruslan Absalyamov
 * @since 2017-03-09
 */
public class BackUpCommand extends BaseBotCommand {
    public BackUpCommand(BotContext context) {
        super("backup", "Pack all files of current user and download", context);
        setOrder(40);
    }

    @Override
    public void execute(AbsSender absSender, Update update, String[] arguments) {
        long chatId = update.getMessage().getChatId();
        User user = update.getMessage().getFrom();
        UserStorage storage = getContext().getUserStorage(user);

        sendMessage(chatId, "Backing up your files...");

        File file = storage.createBackFile();

        if (file.exists()) {
            getContext().sendFile(chatId, file);
            file.delete();
        } else {
            sendMessage(chatId, "Failed to create backup file! \uD83D\uDE25");
        }
    }
}
