package io.arusland.matebot.web;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Ruslan Absalyamov
 * @since 2017-04-25
 */
public class StatsHelper {
    private final List<StatItem> items = new ArrayList<>();

    public void addItem(HttpServletRequest request) {
        String agent = request.getHeader("User-Agent");

        if (agent == null || agent.isEmpty()) {
            agent = request.getHeader("User-agent");
        }

        items.add(new StatItem(new Date(), agent));
    }

    public List<StatItem> getItems() {
        return items;
    }
}
