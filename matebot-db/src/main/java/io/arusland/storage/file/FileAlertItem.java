package io.arusland.storage.file;

import io.arusland.storage.AlertItem;
import io.arusland.storage.ItemPath;
import io.arusland.storage.ItemType;
import io.arusland.storage.User;
import io.arusland.storage.util.DateValidator;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by ruslan on 10.12.2016.
 */
public class FileAlertItem extends FileItem implements AlertItem {
    private final AlertInfo info;
    private Date nextDate;
    private String title;
    private boolean isActive;

    public FileAlertItem(User user, ItemType type, File file, ItemPath path, AlertInfo info) {
        super(user, type, file, path);
        this.info = Validate.notNull(info, "info");
    }

    @Override
    public Date nextTime() {
        calcNextState();

        return nextDate;
    }

    @Override
    public String getMessage() {
        return info.message;
    }

    @Override
    public String getTitle() {
        calcNextState();

        return title;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    public AlertInfo getInfo() {
        return info;
    }

    private void calcNextState() {
        Calendar alertTime = Calendar.getInstance();
        long nowMillis = alertTime.getTimeInMillis();
        alertTime.set(Calendar.HOUR_OF_DAY, info.hour);
        alertTime.set(Calendar.MINUTE, info.minute);

        if (info.weekDays > 0) {
            List<Integer> alertDays = getAlertDays(info.weekDays);

            while (true) {
                int dayOfWeek = getDayOfWeek(alertTime);

                if (alertDays.contains(dayOfWeek) && alertTime.getTimeInMillis() > nowMillis) {
                    refreshState(alertTime.getTime());
                    return;
                }

                alertTime.add(Calendar.HOUR_OF_DAY, 24);
            }
        }

        if (info.day != null) {
            if (info.month != null) {
                if (info.year != null) {
                    alertTime.set(Calendar.YEAR, info.year);
                    alertTime.set(Calendar.MONTH, info.month - 1);
                    alertTime.set(Calendar.DAY_OF_MONTH, info.day);
                    refreshState(alertTime.getTime());
                    return;
                } else {
                    int year = alertTime.get(Calendar.YEAR);
                    while (!DateValidator.isValid(info.day, info.month, year)) {
                        year++;
                    }
                    alertTime.set(Calendar.YEAR, year);
                    refreshState(alertTime.getTime());
                    return;
                }
            } else {
                // TODO: implement!!!!
            }
        }
    }

    private void refreshState(Date date) {
        nextDate = date;
        isActive = nextDate.getTime() > System.currentTimeMillis();
    }

    private static List<Integer> getAlertDays(int flags) {
        List<Integer> result = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            if ((flags & (1 << i)) > 0) {
                result.add(i + 1);
            }
        }

        return result;
    }

    /**
     * Convert to value when monday is first.
     */
    private static int getDayOfWeek(Calendar cal) {
        int day = cal.get(Calendar.DAY_OF_WEEK);
        return day == Calendar.SUNDAY ? 7 : day - 1;
    }
}
