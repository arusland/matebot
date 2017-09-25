package io.arusland.bots.commands;

import io.arusland.bots.base.BotContext;
import io.arusland.bots.utils.TimeUtils;
import org.telegram.telegrambots.api.objects.User;

import java.util.Date;

/**
 * Show alerts til the end of current month.
 *
 * @author Ruslan Absalyamov
 * @since 2017-09-24
 */
public class ShowAlertsMonth extends ShowAlertsBase {
    public ShowAlertsMonth(BotContext context) {
        super("month", "Show alerts for current month", context);
    }

    @Override
    protected Date getPeriodEnd(User user) {
        Date nowClient = getContext().toClient(user, new Date());
        Date monthEnd = getContext().fromClient(user, TimeUtils.getMonthEnd(nowClient));
        return monthEnd;
    }

    @Override
    protected String getTitle() {
        return "Alerts til the end of current month";
    }
}
