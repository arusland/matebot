package io.arusland.bots.utils;

import io.arusland.bots.BotConfig;
import io.arusland.storage.*;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.function.BiConsumer;

import static java.util.stream.Collectors.toList;

/**
 * Created by ruslan on 17.12.2016.
 */
public class AlertsRunner {
    private final Logger log = Logger.getLogger(AlertsRunner.class);
    private final Storage storage;
    private final TimeManagement timeManagement;
    private final Map<AlertItem, Date> alerts = Collections.synchronizedMap(new IdentityHashMap<>());
    private final BiConsumer<AlertItem, Long> alertFiredOnUserIdHandler;
    private final BotConfig botConfig;

    public AlertsRunner(Storage storage, TimeManagement timeManagement,
                        BiConsumer<AlertItem, Long> alertFiredOnUserIdHandler, BotConfig botConfig) {
        this.storage = Validate.notNull(storage, "storage");
        this.timeManagement = Validate.notNull(timeManagement, "timeManagement");
        this.alertFiredOnUserIdHandler = Validate.notNull(alertFiredOnUserIdHandler, "alertFiredOnUserIdHandler");
        this.botConfig = Validate.notNull(botConfig, "botConfig");
    }

    public void rerunAlerts() {
        long lastTime = System.currentTimeMillis();
        log.info("Rerun alerts...");
        removeAllAlerts();

        List<User> users = storage.listUsers();

        for (User user : users) {
            UserStorage ustorage = storage.getOrCreate(user);
            ustorage.setTimeZone(botConfig.getUserTimeZone(user.getId()));
            List<AlertItem> alerts = ustorage.getItemByPath(ItemType.ALERTS)
                    .listItems();
            List<AlertItem> activeAlerts = alerts.stream()
                    .filter(p -> p.isActive())
                    .collect(toList());

            log.info(String.format("Alerts found for user %d (%s), all: %d, active: %d",
                    user.getId(), user.getName(), alerts.size(), activeAlerts.size()));

            if (!activeAlerts.isEmpty()) {
                activeAlerts.forEach(a -> enqueueAlert(a, user.getId()));
            }
        }

        log.info("Alerts run in " + (System.currentTimeMillis() - lastTime) + " ms");
    }

    public void removeAllAlerts() {
        List<AlertItem> alertsToRemove = new ArrayList<>(alerts.keySet());
        alertsToRemove.forEach(a -> dequeueAlert(a));
    }

    public List<AlertItem> nextAlerts(Date dateTo) {
        if (dateTo == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, 1000);
            dateTo = cal.getTime();
        }

        final Date dateToReal = dateTo;

        List<AlertItem> nextAlerts = new ArrayList<>(alerts.keySet()).stream()
                .filter(p -> {
                    Date nextDate = p.nextTime();

                    return nextDate != null && nextDate.before(dateToReal);
                }).collect(toList());

        nextAlerts.sort(new Comparator<AlertItem>() {
            @Override
            public int compare(AlertItem o1, AlertItem o2) {
                Date dt1 = o1.nextTime();
                Date dt2 = o2.nextTime();

                if (dt1 != null && dt2 != null) {
                    return dt1.compareTo(dt2);
                }

                return 0;
            }
        });


        return nextAlerts;
    }

    private void enqueueAlert(AlertItem alert, long userId) {
        enqueueAlert(alert, () -> {
            alertFiredOnUserIdHandler.accept(alert, userId);

            if (alert.isActive()) {
                // if alert is active enqueue it again
                enqueueAlert(alert, userId);
            }
        });
    }

    private void enqueueAlert(AlertItem alert, Runnable handler) {
        Date time = alert.nextTime();

        if (time != null) {
            log.info("Alert added to queue: " + alert);

            alerts.put(alert, time);
            timeManagement.enqueue(time, () -> {
                log.info("Alert fired: " + alert);
                alerts.remove(time);
                handler.run();
            });
        }
    }

    private void dequeueAlert(AlertItem alert) {
        Date time = alerts.get(alert);

        if (time != null) {
            log.info("Remove alert from queue: " + alert);
            alerts.remove(alert);
            timeManagement.dequeue(time);
        }
    }
}
