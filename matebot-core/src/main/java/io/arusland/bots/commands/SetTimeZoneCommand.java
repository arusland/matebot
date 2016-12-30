package io.arusland.bots.commands;

import io.arusland.bots.base.BaseBotCommand;
import io.arusland.bots.base.BotContext;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.AbsSender;

import java.util.TimeZone;

/**
 * Created by ruslan on 30.12.2016.
 */
public class SetTimeZoneCommand extends BaseBotCommand {
    public SetTimeZoneCommand(BotContext context) {
        super("tz", "Set time zone (e.g. +5)", context);
    }

    @Override
    public void execute(AbsSender absSender, Update update, String[] arguments) {
        TimeZone timeZone = parseTimeZone(arguments);
        if (timeZone != null) {
            getContext().setTimeZone(timeZone);
        } else {
            sendMessage(update.getMessage().getChatId(), "Wrong timezone format");
        }
    }

    private TimeZone parseTimeZone(String[] arguments) {
        return null;
    }
}
