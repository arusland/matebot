package io.arusland.matebot.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.util.logging.Logger;

public class StartupListener implements ServletContextListener {
    private final Logger log = Logger.getLogger(StartupListener.class.getName());

    @Override
    public void contextDestroyed(ServletContextEvent ev) {
        log.info("Context stopping...");
    }

    @Override
    public void contextInitialized(ServletContextEvent ev) {
        log.info("Bot session starting...");
        try {
            OpenshiftRunner.run();
        } catch (IOException e) {
            e.printStackTrace();
            log.warning(e.toString());
        } catch (InterruptedException e) {
            log.warning(e.toString());
            e.printStackTrace();
        }
    }
}
