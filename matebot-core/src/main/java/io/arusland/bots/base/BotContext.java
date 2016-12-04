package io.arusland.bots.base;

import io.arusland.bots.commands.ShortcutCommand;
import io.arusland.storage.UserStorage;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;

import java.io.File;
import java.util.List;

/**
 * Created by ruslan on 03.12.2016.
 */
public interface BotContext {
    List<BaseBotCommand> getRegisteredCommands();

    UserStorage getUserStorage(User user);

    void setCurrentDir(User user, String path);

    String getCurrentPath(User user);

    void listCurrentDir(Update update);

    void sendMessage(Long chatId, SendMessage message);

    void sendFile(Long chatId, File file);

    void showRoots(Update update);

    /**
     * Adds shortcut for command with arguments for specified user.
     *
     * @param user User.
     * @param shortcut Shortcut for command, e.g. <code>/ls</code>.
     * @param cmd Command.
     * @param args Command's arguments. Could be empty.
     */
    void addShortcutCommand(User user, String shortcut, String cmd, String... args);

    /**
     * Gets command with arguments.
     * @param user User.
     * @param shortcut Shortcut for command.
     * @return Shortcut command.
     */
    ShortcutCommand getShortcutCommand(User user, String shortcut);
}
