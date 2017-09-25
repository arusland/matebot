package io.arusland.bots.commands;

import io.arusland.bots.base.BaseBotCommand;
import io.arusland.bots.base.BotContext;
import io.arusland.bots.utils.TimeUtils;
import io.arusland.storage.AlertItem;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ruslan Absalyamov
 * @since 2017-09-23
 */
public abstract class ShowAlertsBase extends BaseBotCommand {
    private static final String EMOJI_BELL = "\uD83D\uDD14";
    private static final String EMOJI_DIR = "\uD83D\uDCC1";

    public ShowAlertsBase(String commandIdentifier, String description, BotContext context) {
        super(commandIdentifier, description, context);
    }

    protected abstract Date getPeriodEnd(User user);

    protected abstract String getTitle();

    protected String getAlertTitle(AlertItem alert, User user) {
        return alert.getTitle();
    }

    @Override
    public void execute(AbsSender absSender, Update update, String[] arguments) {
        long chatId = update.getMessage().getChat().getId();
        User user = update.getMessage().getFrom();

        Date periodEnd = getPeriodEnd(user);
        List<AlertItem> alerts = getContext()
                .nextAlerts(periodEnd)
                .stream()
                .filter(p -> p.isActive() && p.getUser().getId() == user.getId())
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();

        if (alerts.isEmpty()) {
            sb.append("Active alerts not found");
        } else {
            sb.append(getTitle());
            sb.append('\n');
            for (int i = 0; i < alerts.size(); i++) {
                renderAlertItem(user, sb, i, alerts.get(i));
            }
        }

        sendMessage(chatId, sb.toString());
    }

    private void renderAlertItem(User user, StringBuilder sb, int index, AlertItem alert) {
        String changeDirShortcut = "/" + (index + 1);
        String removeFileShortcut = "/del" + (index + 1);
        sb.append(changeDirShortcut);
        sb.append(" ");

        if (alert.isDirectory()) {
            sb.append(EMOJI_DIR);
        } else {
            if (alert.isActive()) {
                sb.append(EMOJI_BELL);
            } else {
                sb.append("❌");
            }
        }

        Date nextTime = alert.nextTime();
        if (nextTime != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(nextTime);
            sb.append(getAlertTitle(alert, user));
            String span = TimeUtils.friendlyTimespanShort(nextTime);
            if (!span.isEmpty()) {
                sb.append(" (" + span + ") ");
            }
        } else {
            sb.append(alert.getTitle());
        }

        if (!alert.isDirectory()) {
            sb.append(" ");
            sb.append(removeFileShortcut);
        }

        sb.append("\n");

        if (alert.isDirectory()) {
            getContext().addShortcutCommand(user.getId(), changeDirShortcut, "cd", alert.getFullPath());
        } else {
            getContext().addShortcutCommand(user.getId(), changeDirShortcut, "dl", alert.getFullPath());
            getContext().addShortcutCommand(user.getId(), removeFileShortcut, "rm", alert.getFullPath());
        }
    }
}
