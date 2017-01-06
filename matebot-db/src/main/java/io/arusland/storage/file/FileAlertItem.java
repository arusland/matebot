package io.arusland.storage.file;

import io.arusland.storage.*;
import io.arusland.storage.util.DateValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by ruslan on 10.12.2016.
 */
public class FileAlertItem extends FileItem<AlertItem> implements AlertItem {
    private final static Logger log = Logger.getLogger(FileAlertItem.class);
    private static final int MESSAGE_TITLE_LENGTH = 10;
    private final AlertInfo info;
    private final TimeZoneClient timeZoneClient;
    private Date nextDate;
    private String title;

    public FileAlertItem(User user, File file, ItemPath path, AlertInfo info,
                         ItemFactory itemFactory, TimeZoneClient timeZoneClient) {
        super(user, ItemType.ALERTS, file, path, itemFactory);
        this.info = Validate.notNull(info, "info");
        this.timeZoneClient = Validate.notNull(timeZoneClient, "timeZoneClient");
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
    public String getSource() {
        return info.content;
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

        Calendar alertClientTime = Calendar.getInstance();
        alertClientTime.setTime(timeZoneClient.toClient(alertClientTime.getTime()));
        long nowClientMillis = alertClientTime.getTimeInMillis();
        alertClientTime.set(Calendar.SECOND, 0);
        alertClientTime.set(Calendar.MILLISECOND, 0);
        alertClientTime.set(Calendar.HOUR_OF_DAY, info.hour);
        alertClientTime.set(Calendar.MINUTE, info.minute);

        if (info.weekDays > 0) {
            List<Integer> alertDays = getAlertDays(info.weekDays);

            while (true) {
                int dayOfWeek = getDayOfWeek(alertClientTime);

                if (alertDays.contains(dayOfWeek) && alertClientTime.getTimeInMillis() > nowClientMillis) {
                    refreshState(alertClientTime);
                    return;
                }

                alertClientTime.add(Calendar.HOUR_OF_DAY, 24);
            }
        }

        if (info.day != null) {
            if (info.month != null) {
                if (info.year != null) {
                    // if day, month and year defined
                    alertClientTime.set(Calendar.YEAR, info.year);
                    alertClientTime.set(Calendar.MONTH, info.month - 1);
                    alertClientTime.set(Calendar.DAY_OF_MONTH, info.day);
                    refreshState(alertClientTime);
                    return;
                } else {
                    // if day and month defined
                    int year = alertClientTime.get(Calendar.YEAR);
                    while (!DateValidator.isValid(info.day, info.month, year)) {
                        year++;
                    }
                    alertClientTime.set(Calendar.YEAR, year);
                    alertClientTime.set(Calendar.MONTH, info.month - 1);
                    alertClientTime.set(Calendar.DAY_OF_MONTH, info.day);
                    refreshState(alertClientTime);
                    return;
                }
            } else {
                // if only day defined
                int year = alertClientTime.get(Calendar.YEAR);
                int month = alertClientTime.get(Calendar.MONTH) + 1;

                while (true) {
                    if (DateValidator.isValid(info.day, month, year)) {
                        alertClientTime.set(Calendar.YEAR, year);
                        alertClientTime.set(Calendar.MONTH, month - 1);
                        alertClientTime.set(Calendar.DAY_OF_MONTH, info.day);

                        if (alertClientTime.getTimeInMillis() > nowClientMillis) {
                            refreshState(alertClientTime);
                            return;
                        }
                    }

                    month++;

                    if (month > 12) {
                        month = 1;
                        year++;
                    }
                }
            }
        }
    }

    private void refreshState(Calendar clientTime) {
        String newTitle = String.format("%02d:%02d %02d:%02d:%d",
                info.hour, info.minute,
                clientTime.get(Calendar.DAY_OF_MONTH),
                clientTime.get(Calendar.MONTH) + 1,
                clientTime.get(Calendar.YEAR));

        if (StringUtils.isNotBlank(getMessage())) {
            newTitle += " " + StringUtils.abbreviate(getMessage(), MESSAGE_TITLE_LENGTH);
        }

        this.title = newTitle;
        this.nextDate = timeZoneClient.fromClient(clientTime.getTime());

        log.info(this);
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
