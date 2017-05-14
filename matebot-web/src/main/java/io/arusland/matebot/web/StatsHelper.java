package io.arusland.matebot.web;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Ruslan Absalyamov
 * @since 2017-04-25
 */
public class StatsHelper {
    private final SimpleDateFormat sdfClient = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public StatsHelper() {
        sdfClient.setTimeZone(TimeZone.getTimeZone("GMT+3"));
    }

    private final List<StatItem> items = new ArrayList<>();

    public void addItem(HttpServletRequest request) {
        String agent = request.getHeader("User-Agent");

        if (agent == null || agent.isEmpty()) {
            agent = request.getHeader("User-agent");
        }

        items.add(0, new StatItem(toClient(new Date()), request.getRemoteAddr(), agent));
    }

    public List<StatItem> getItems() {
        return items;
    }

    public synchronized Date toClient(Date localTime) {
        try {
            return sdfLocal.parse(sdfClient.format(localTime));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
