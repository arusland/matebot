package io.arusland.bots.commands;

import io.arusland.bots.base.BaseBotCommand;
import io.arusland.bots.base.BotContext;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.AbsSender;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Kills current jvm.
 * <p>
 * Created by ruslan on 26.12.2016.
 */
public class KillBotCommand extends BaseBotCommand {
    public KillBotCommand(BotContext context) {
        super("kill", "Kills current bot", context);
        setOrder(40);
    }

    @Override
    public void execute(AbsSender absSender, Update update, String[] arguments) {
        sendMessage(update.getMessage().getChatId(), "Bye bye!");
        log.warn("Killing jvm...");
        System.exit(1);
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
