package io.arusland.matebot.web;

import io.arusland.bots.MateBot;
import io.arusland.bots.utils.AlertsRunner;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.updatesreceivers.BotSession;

public class StartupListener implements ServletContextListener {
    private BotSession botSession;

    private final Logger log = Logger.getLogger(AlertsRunner.class);

    @Override
    public void contextDestroyed(ServletContextEvent ev) {
        log.info("Context stopping...");

        if (botSession != null) {
            botSession.close();
            log.info("Bot session stopped");
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent ev) {
        log.info("Bot session starting...");
        botSession = MateBot.start(new String[0]);
    }
}
