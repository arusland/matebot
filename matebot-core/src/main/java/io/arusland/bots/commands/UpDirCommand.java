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

/**
 * Created by ruslan on 04.12.2016.
 */
public class UpDirCommand extends BaseBotCommand {
    /**
     * @param context Context for current bot.
     */
    public UpDirCommand(BotContext context) {
        super("up", "Go to the parent dir", context);
        setOrder(30);
    }

    @Override
    public void execute(AbsSender absSender, Update update, String[] arguments) {
        User user = update.getMessage().getFrom();
        UserStorage storage = getContext().getUserStorage(user);
        String currentPath = getContext().getCurrentPath(user);

        if (StringUtils.isBlank(currentPath)) {
            getContext().showRoots(update);
        } else {
            Item currentItem = storage.getItemByPath(currentPath);

            if (currentItem == null) {
                getContext().setCurrentDir(user, "");
                getContext().showRoots(update);
            } else {
                getContext().setCurrentDir(user, currentItem.getParentPath());
                getContext().listCurrentDir(update);
            }
        }
    }
}
