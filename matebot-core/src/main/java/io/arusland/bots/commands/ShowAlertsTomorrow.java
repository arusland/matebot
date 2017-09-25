package io.arusland.bots.commands;

import io.arusland.bots.base.BotContext;
import io.arusland.bots.utils.TimeUtils;
import org.telegram.telegrambots.api.objects.User;

import java.util.Calendar;
import java.util.Date;

/**
 * Show alerts til the end of next day.
 *
 * @author Ruslan Absalyamov
 * @since 2017-09-23
 */
public class ShowAlertsTomorrow extends ShowAlertsBase {
    public ShowAlertsTomorrow(BotContext context) {
        super("tomorrow", "Show alerts for tomorrow", context);
    }

    @Override
    protected Date getPeriodEnd(User user) {
        Date nowClient = getContext().toClient(user, new Date());
        Date todayEnd = TimeUtils.getTodayEnd(nowClient);
        Calendar cal = Calendar.getInstance();
        cal.setTime(todayEnd);
        cal.add(Calendar.DAY_OF_MONTH, 1);

        return getContext().fromClient(user, cal.getTime());
    }

    @Override
    protected String getTitle() {
        return "Alerts til the end of the next day";
    }
}
