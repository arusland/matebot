package io.arusland.storage.file;

import io.arusland.storage.*;
import io.arusland.storage.util.DateValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

/**
 * Created by ruslan on 10.12.2016.
 */
public class FileAlertItem extends FileItem<AlertItem> implements AlertItem {
    private final static Logger log = Logger.getLogger(FileAlertItem.class);
    private static final DateFormat DF = new SimpleDateFormat("HH:mm dd.MM.yyyy");
    private static final int MESSAGE_TITLE_LENGTH = 40;
    private final AlertInfo info;
    private final TimeZoneClient timeZoneClient;
    private final Consumer<FileAlertItem> onLastActivePeriodUpdate;
    private Date nextDate;
    private String title;
    private Date lastActivePeriodTime;
    private boolean isPeriodActive;

    public FileAlertItem(User user, File file, ItemPath path, AlertInfo info,
                         Date lastActivePeriodTime, ItemFactory itemFactory,
                         TimeZoneClient timeZoneClient,
                         Consumer<FileAlertItem> onLastActivePeriodUpdate) {
        super(user, ItemType.ALERTS, file, path, itemFactory);
        this.info = Validate.notNull(info, "info");
        this.lastActivePeriodTime = lastActivePeriodTime;
        this.timeZoneClient = Validate.notNull(timeZoneClient, "timeZoneClient");
        this.onLastActivePeriodUpdate = Validate.notNull(onLastActivePeriodUpdate, "onLastActivePeriodUpdate");
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
    public String getMessageShort() {
        return StringUtils.abbreviate(info.message, MESSAGE_TITLE_LENGTH);
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

        return nextDate != null && currentDateSupplier.get().before(nextDate);
    }

    @Override
    public boolean isPeriodActive() {
        return isActive() && isPeriodActive;
    }

    @Override
    public void cancelActivePeriod() {
        lastActivePeriodTime = null;
        nextDate = null;
        isPeriodActive = false;
        calcNextState();
        onLastActivePeriodUpdate.accept(this);
    }

    @Override
    public Integer getPeriod() {
        return info.period;
    }

    @Override
    public ChronoUnit getPeriodType() {
        return info.periodType;
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
            if (nextDate.after(currentDateSupplier.get())) {
                log.info(String.format("Got cached nextDate: %s", formatDate(timeZoneClient.toClient(nextDate))));
                return;
            }
        }

        Calendar alertClientTime = Calendar.getInstance();
        alertClientTime.setTime(currentDateSupplier.get());
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
                    log.info(String.format("[DayOfWeek] nextDate: %s", formatDate(alertClientTime.getTime())));
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
                    log.info(String.format("[day month year] nextDate: %s",
                            formatDate(alertClientTime.getTime())));
                    refreshState(alertClientTime);
                    return;
                } else {
                    // if day and month defined
                    int year = alertClientTime.get(Calendar.YEAR);

                    while (true) {
                        if (DateValidator.isValid(info.day, info.month, year)) {
                            alertClientTime.set(Calendar.YEAR, year);
                            alertClientTime.set(Calendar.MONTH, info.month - 1);
                            alertClientTime.set(Calendar.DAY_OF_MONTH, info.day);

                            if (alertClientTime.getTimeInMillis() > nowClientMillis) {
                                log.info(String.format("[day month] nextDate: %s",
                                        formatDate(alertClientTime.getTime())));
                                refreshState(alertClientTime);
                                return;
                            }
                        }
                        year++;
                    }
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
                            log.info(String.format("[day] nextDate: %s",
                                    formatDate(alertClientTime.getTime())));

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

    /**
     * Beginning of active period. Server time (not client).
     */
    public Date getLastActivePeriodTime() {
        return lastActivePeriodTime;
    }

    /**
     * Beginning of active period. Server time (not client).
     */
    public void setLastActivePeriodTime(Date lastActivePeriodTime) {
        this.lastActivePeriodTime = lastActivePeriodTime;
    }

    private void refreshState(Calendar clientTime) {
        this.nextDate = timeZoneClient.fromClient(clientTime.getTime());

        usePeriodIfSet();

        String newTitle = String.format("%02d:%02d %02d:%02d:%d",
                info.hour, info.minute,
                clientTime.get(Calendar.DAY_OF_MONTH),
                clientTime.get(Calendar.MONTH) + 1,
                clientTime.get(Calendar.YEAR));

        if (StringUtils.isNotBlank(getMessage())) {
            newTitle += " " + StringUtils.abbreviate(getMessage(), MESSAGE_TITLE_LENGTH);
        }


        this.title = newTitle;

        log.info(this);
    }

    private void usePeriodIfSet() {
        if (info.period != null) {
            long now = currentDateSupplier.get().getTime();
            boolean isActive = nextDate.getTime() > now;
            long timeInMs = calcPeriodInMs(info.period, info.periodType, nextDate);

            if (isActive) {
                // when launch first time
                if (lastActivePeriodTime == null) {
                    lastActivePeriodTime = new Date(nextDate.getTime() + timeInMs);

                    if (log.isInfoEnabled()) {
                        log.info(String.format("[period] first init lastActivePeriodTime: %s",
                                timeZoneClient.toClient(lastActivePeriodTime)));
                    }
                } else {
                    lastActivePeriodTime = recalcNextActivePeriodTime(lastActivePeriodTime, now, timeInMs);

                    if (lastActivePeriodTime.before(nextDate)) {
                        nextDate = lastActivePeriodTime;
                        isPeriodActive = true;

                        if (log.isInfoEnabled()) {
                            log.info(String.format("[period] period active, lastActivePeriodTime/nextDate: %s",
                                    formatDate(timeZoneClient.toClient(lastActivePeriodTime))));
                        }
                    } else {
                        // recalc new active period time
                        lastActivePeriodTime = new Date(nextDate.getTime() + timeInMs);
                        isPeriodActive = false;

                        if (log.isInfoEnabled()) {
                            log.info(String.format("[period] period not active, lastActivePeriodTime: %s, nextDate: %s",
                                    formatDate(timeZoneClient.toClient(lastActivePeriodTime)),
                                    formatDate(timeZoneClient.toClient(nextDate))));
                        }
                    }
                }

                onLastActivePeriodUpdate.accept(this);
            } else if (lastActivePeriodTime != null) {
                lastActivePeriodTime = recalcNextActivePeriodTime(lastActivePeriodTime, now, timeInMs);
                nextDate = lastActivePeriodTime;
                isPeriodActive = true;

                if (log.isInfoEnabled()) {
                    log.info(String.format("[period] only period active, lastActivePeriodTime: %s",
                            formatDate(timeZoneClient.toClient(lastActivePeriodTime))));
                }

                onLastActivePeriodUpdate.accept(this);
            }
        }
    }

    private Date recalcNextActivePeriodTime(Date nextActivePeriodTime, long now, long timeInMs) {
        if (nextActivePeriodTime.getTime() <= now) {
            long lastPeriod = nextActivePeriodTime.getTime();

            do {
                lastPeriod += timeInMs;
            } while (lastPeriod <= now);

            nextActivePeriodTime = new Date(lastPeriod);
        }

        log.info(String.format("[period] recalc next lastActivePeriodTime: %s",
                formatDate(timeZoneClient.toClient(nextActivePeriodTime))));

        return nextActivePeriodTime;
    }

    private long calcPeriodInMs(Integer period, ChronoUnit periodType, Date nextDate) {
        if (periodType == ChronoUnit.MONTHS) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(nextDate);
            cal.add(Calendar.MONTH, period);

            return cal.getTimeInMillis() - nextDate.getTime();
        } else if (periodType == ChronoUnit.YEARS) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(nextDate);
            cal.add(Calendar.YEAR, period);

            return cal.getTimeInMillis() - nextDate.getTime();
        } else
            return Duration.of(period, periodType).get(ChronoUnit.SECONDS) * 1000;
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

    private static String formatDate(Date dt) {
        synchronized (DF) {
            return DF.format(dt);
        }
    }

    @Override
    public String toString() {
        return "FileAlertItem{" +
                "nextDate=" + nextDate +
                ", title='" + title + '\'' +
                '}';
    }

    protected static void configure(Supplier<Date> currentDateSupplier) {
        FileAlertItem.currentDateSupplier = currentDateSupplier;
    }

    private static Supplier<Date> currentDateSupplier = () -> new Date();
}
