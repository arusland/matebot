package io.arusland.bots.commands;

import io.arusland.bots.base.BotContext;
import io.arusland.bots.utils.TimeUtils;
import io.arusland.storage.AlertItem;

import java.util.Calendar;
import java.util.Date;

/**
 * Show today's alerts
 *
 * @author Ruslan Absalyamov
 * @since 2017-09-23
 */
public class ShowAlertsToday extends ShowAlertsBase {
    public ShowAlertsToday(BotContext context) {
        super("today", "Show today's alerts", context);
    }

    @Override
    protected Date getPeriodEnd() {
        return TimeUtils.getTodayEnd();
    }

    @Override
    protected String getTitle() {
        return "Alerts for today";
    }

    @Override
    protected String getAlertTitle(AlertItem alert) {
        Date nextTime = alert.nextTime();

        if (nextTime != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(nextTime);
            String newTitle = String.format("%02d:%02d ",
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
            return newTitle + alert.getMessageShort();
        }

        return super.getAlertTitle(alert);
    }
}
