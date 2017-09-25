package io.arusland.bots.commands;

import io.arusland.bots.base.BotContext;
import io.arusland.bots.utils.TimeUtils;
import org.telegram.telegrambots.api.objects.User;

import java.util.Date;

/**
 * Show alerts til the end of current week.
 *
 * @author Ruslan Absalyamov
 * @since 2017-09-23
 */
public class ShowAlertsWeek extends ShowAlertsBase {
    public ShowAlertsWeek(BotContext context) {
        super("week", "Show alerts for current week", context);
    }

    @Override
    protected Date getPeriodEnd(User user) {
        Date nowClient = getContext().toClient(user, new Date());
        Date weekEnd = getContext().fromClient(user, TimeUtils.getWeekEnd(nowClient));
        return weekEnd;
    }

    @Override
    protected String getTitle() {
        return "Alerts til the end of current week";
    }
}
