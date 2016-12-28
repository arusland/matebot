package io.arusland.matebot.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
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
        try {
            OpenshiftRunner.run();
            timer.schedule(new RequestTask(), 0, TimeUnit.HOURS.toMillis(1));
        } catch (IOException e) {
            e.printStackTrace();
            log.warning(e.toString());
        } catch (InterruptedException e) {
            log.warning(e.toString());
            e.printStackTrace();
        }
    }

    // please, openshift, don't sleep!
    private class RequestTask extends TimerTask {
        @Override
        public void run() {
            String content = downloadPage("YOUR VALID OPENSHIFT URL");
            if (!content.isEmpty()) {
                Statistics.incrementPageViewCount();
            }
        }
    }

    private String downloadPage(String urlLink) {
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;
        StringBuilder sb = new StringBuilder();

        try {
            url = new URL(urlLink);
            System.out.println("Downloading link - " + url);
            is = url.openStream();
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ioe) {
            }
        }

        return sb.toString();
    }
}
