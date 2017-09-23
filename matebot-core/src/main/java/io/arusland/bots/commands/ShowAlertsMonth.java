package io.arusland.bots.commands;

import io.arusland.bots.base.BotContext;
import io.arusland.bots.utils.TimeUtils;

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
    protected Date getPeriodEnd() {
        return TimeUtils.getMonthEnd();
    }

    @Override
    protected String getTitle() {
        return "Alerts til the end of current month";
    }
}
