package io.arusland.bots.commands;

import io.arusland.bots.base.BaseBotCommand;
import io.arusland.bots.base.BotContext;
import io.arusland.bots.utils.ExecUtils;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.AbsSender;

import java.io.IOException;

/**
 *
 * Show java processes list.
 *
 * @author Ruslan Absalyamov
 * @since 2017-04-03
 */
public class ShowProcessList extends BaseBotCommand {
    public ShowProcessList(BotContext context) {
        super("ps", "Show java processes list", context);
    }

    @Override
    public void execute(AbsSender absSender, Update update, String[] arguments) {
        long chatId = update.getMessage().getChat().getId();

        try {
            String output = ExecUtils.runCommand("jps");
            sendMessage(chatId, output);
        } catch (Exception e) {
            sendMessage(chatId, "EXCEPTION: " +e.getMessage());
        }
    }

    @Override
    public boolean isVisible() {
        return false;
    }
}
