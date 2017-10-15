package io.arusland.bots.base;

import io.arusland.storage.Item;
import io.arusland.storage.UserStorage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;

import javax.validation.constraints.NotNull;

/**
 * Created by ruslan on 03.12.2016.
 */
public abstract class BaseBotCommand extends BotCommand {
    protected final Logger log = Logger.getLogger(getClass());
    private final BotContext context;
    private int order = 50;

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     * @param context           Context for current bot.
     */
    public BaseBotCommand(String commandIdentifier, String description, BotContext context) {
        super(commandIdentifier, description);
        this.context = Validate.notNull(context, "context");
    }

    public abstract void execute(AbsSender absSender, Update update, String[] arguments);

    @Override
    public final void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        throw new RuntimeException("Don't call this method!");
    }

    public BotContext getContext() {
        return context;
    }

    public void sendMessage(Long chatId, String msg) {
        context.sendMessage(chatId, msg);
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Item getCurrentItem(User user) {
        String currentPath = getContext().getCurrentPath(user);
        UserStorage storage = getContext().getUserStorage(user);

        if (StringUtils.isNoneBlank(currentPath)) {
            return storage.getItemByPath(currentPath);
        }

        return null;
    }

    @NotNull
    public String getCurrentPath(User user) {
        String currentPath = getContext().getCurrentPath(user);

        return StringUtils.defaultString(currentPath, "/");
    }

    /**
     * true if command must be visible.
     */
    public boolean isVisible() {
        return true;
    }
}
