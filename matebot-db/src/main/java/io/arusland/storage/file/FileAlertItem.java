package io.arusland.storage.file;

import io.arusland.storage.*;
import io.arusland.storage.util.DateValidator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by ruslan on 10.12.2016.
 */
public class FileAlertItem extends FileItem<AlertItem> implements AlertItem {
    private static final int MESSAGE_TITLE_LENGTH = 10;
    private final AlertInfo info;
    private Date nextDate;
    private String title;

    public FileAlertItem(User user, File file, ItemPath path, AlertInfo info, ItemFactory itemFactory) {
        super(user, ItemType.ALERTS, file, path, itemFactory);
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
        calcNextState();

        return nextDate != null && System.currentTimeMillis() < nextDate.getTime();
    }

    @Override
    public List<AlertItem> listItems() {
        if (isDirectory()) {
            List<AlertItem> result = Arrays.stream(getFile()
                    .listFiles(TypedFileFilter.get(ItemType.ALERTS)))
                    .map(f -> getItemFactory().fromFile(ItemType.ALERTS, f, ItemPath.parse(getFullPath() + "/" + f.getName())))
                    .filter(p -> p.isPresent())
                    .map(p -> (AlertItem) p.get())
                    .collect(toList());

            result.sort(this);

            return result;
        }

        return Collections.emptyList();
    }

    public AlertInfo getInfo() {
        return info;
    }

    private void calcNextState() {
        if (nextDate != null) {
            if (nextDate.getTime() > System.currentTimeMillis()) {
                return;
            }
        }

        Calendar alertTime = Calendar.getInstance();
        long nowMillis = alertTime.getTimeInMillis();
        alertTime.set(Calendar.SECOND, 0);
        alertTime.set(Calendar.MILLISECOND, 0);
        alertTime.set(Calendar.HOUR_OF_DAY, info.hour);
        alertTime.set(Calendar.MINUTE, info.minute);

        if (info.weekDays > 0) {
            List<Integer> alertDays = getAlertDays(info.weekDays);

            while (true) {
                int dayOfWeek = getDayOfWeek(alertTime);

                if (alertDays.contains(dayOfWeek) && alertTime.getTimeInMillis() > nowMillis) {
                    refreshState(alertTime);
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
                    refreshState(alertTime);
                    return;
                } else {
                    int year = alertTime.get(Calendar.YEAR);
                    while (!DateValidator.isValid(info.day, info.month, year)) {
                        year++;
                    }
                    alertTime.set(Calendar.YEAR, year);
                    refreshState(alertTime);
                    return;
                }
            } else {
                // TODO: implement!!!!
            }
        }
    }

    private void refreshState(Calendar cal) {
        String newTitle = String.format("%d:%d %d:%d:%d",
                info.hour, info.minute,
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR));

        if (StringUtils.isNotBlank(getMessage())) {
            newTitle += " " + StringUtils.abbreviate(getMessage(), MESSAGE_TITLE_LENGTH);
        }

        this.title = newTitle;
        this.nextDate = cal.getTime();
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

    @Override
    public String toString() {
        return "FileAlertItem{" +
                "nextDate=" + nextDate +
                ", title='" + title + '\'' +
                '}';
    }
}
