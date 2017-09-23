package io.arusland.bots.commands;

import io.arusland.bots.base.BotContext;
import io.arusland.bots.utils.TimeUtils;

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
    protected Date getPeriodEnd() {
        return TimeUtils.getWeekEnd();
    }

    @Override
    protected String getTitle() {
        return "Alerts til the end of current week";
    }
}
