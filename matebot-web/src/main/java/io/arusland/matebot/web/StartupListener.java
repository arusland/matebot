package io.arusland.matebot.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class StartupListener implements ServletContextListener {
    private final Logger log = Logger.getLogger(StartupListener.class.getName());
    private final Timer timer = new Timer();

    @Override
    public void contextDestroyed(ServletContextEvent ev) {
        log.info("Context stopping...");
    }

    @Override
    public void contextInitialized(ServletContextEvent ev) {
        log.info("Bot session starting...");
        // every 8 hours restart the bot
        timer.schedule(new MatebotTimerTask(), 1000, 8*60*60*1000);
    }

    private class MatebotTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                OpenshiftRunner.run();
            } catch (IOException e) {
                log.warning(e.toString());
                e.printStackTrace();
            } catch (InterruptedException e) {
                log.warning(e.toString());
                e.printStackTrace();
            }
        }
    }
}
