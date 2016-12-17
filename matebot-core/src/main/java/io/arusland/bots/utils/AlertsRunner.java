package io.arusland.bots.utils;

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

    public AlertsRunner(Storage storage, TimeManagement timeManagement,
                        BiConsumer<AlertItem, Long> alertFiredOnUserIdHandler) {
        this.storage = Validate.notNull(storage, "storage");
        this.timeManagement = Validate.notNull(timeManagement, "timeManagement");
        this.alertFiredOnUserIdHandler = Validate.notNull(alertFiredOnUserIdHandler, "alertFiredOnUserIdHandler");
    }

    public void rerunAlerts() {
        long lastTime = System.currentTimeMillis();
        log.info("Rerun alerts...");
        removeAllAlerts();

        List<User> users = storage.listUsers();

        for (User user : users) {
            UserStorage ustorage = storage.getOrCreate(user);
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

        log.info("Alerts run in " + (System.currentTimeMillis() - lastTime) + " ms" );
    }

    public void removeAllAlerts() {
        List<AlertItem> alertsToRemove = new ArrayList<>(alerts.keySet());
        alertsToRemove.forEach(a -> dequeueAlert(a));
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
