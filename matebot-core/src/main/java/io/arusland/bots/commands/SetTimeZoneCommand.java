package io.arusland.bots.commands;

import io.arusland.bots.base.BaseBotCommand;
import io.arusland.bots.base.BotContext;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ruslan on 30.12.2016.
 */
public class SetTimeZoneCommand extends BaseBotCommand {
    private final Pattern TIMEZONE_PATTERN = Pattern.compile("^(\\+|-)\\d+:\\d\\d$");
    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SetTimeZoneCommand(BotContext context) {
        super("tz", "Set time zone (e.g. +5:00)", context);
        setOrder(30);
    }

    @Override
    public void execute(AbsSender absSender, Update update, String[] arguments) {
        TimeZone timeZone = parseTimeZone(arguments);
        Long chatId = update.getMessage().getChatId();
        User currentUser = update.getMessage().getFrom();

        if (timeZone != null) {
            getContext().setTimeZone(currentUser, timeZone);
            getContext().rerunAlerts();
            sendMessage(chatId, "✅ Time zone was set!");
        } else {
            Date localTime = new Date();
            Date clientTime = getContext().toClient(currentUser, localTime);
            TimeZone clientTimeZone = getContext().getTimeZone(currentUser);

            StringBuilder sb = new StringBuilder();
            sb.append("⏱ Time zone info\n");
            sb.append("Server time zone: ");
            sb.append(TimeZone.getDefault().getDisplayName());
            sb.append("\n");

            if (clientTimeZone != null) {
                sb.append("Client time zone: ");
                sb.append(clientTimeZone.getDisplayName());
                sb.append("\n");
            }

            sb.append("Server time: ");
            sb.append(SDF.format(localTime));
            sb.append("\n");
            sb.append("Client time: ");
            sb.append(SDF.format(clientTime));
            sb.append("\n");

            sendMessage(chatId, sb.toString());
        }
    }

    private TimeZone parseTimeZone(String[] arguments) {
        if (arguments.length > 0) {
            Matcher mc = TIMEZONE_PATTERN.matcher(arguments[0]);

            if (mc.find()) {
                return TimeZone.getTimeZone("GMT" + arguments[0]);
            }
        }

        return null;
    }
}
