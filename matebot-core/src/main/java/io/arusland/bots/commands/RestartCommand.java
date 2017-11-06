package io.arusland.bots.commands;

import io.arusland.bots.base.BaseBotCommand;
import io.arusland.bots.base.BotContext;
import io.arusland.bots.utils.ExecUtils;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.AbsSender;

import java.io.IOException;

/**
 * @author Ruslan Absalyamov
 * @since 2017-11-06
 */
public class RestartCommand extends BaseBotCommand {
    public RestartCommand(BotContext context) {
        super("restart", "Restart the bot", context);
        setOrder(40);
    }

    @Override
    public void execute(AbsSender absSender, Update update, String[] arguments) {
        sendMessage(update.getMessage().getChatId(), "Restarting...");

        try {
            String output = ExecUtils.runCommand("nohup", "sh", "restart.sh");
            log.info(output);
        } catch (IOException e) {
            log.error(e.toString());
        }
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public boolean isAdminCommand() {
        return true;
    }
}
