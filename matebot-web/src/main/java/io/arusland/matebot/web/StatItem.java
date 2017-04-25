package io.arusland.matebot.web;

import java.util.Date;

/**
 * @author Ruslan Absalyamov
 * @since 2017-04-25
 */
public class StatItem {
    private final Date time;
    private final String agent;

    public StatItem(Date time, String agent) {
        this.time = time;
        this.agent = agent;
    }

    public Date getTime() {
        return time;
    }

    public String getAgent() {
        return agent;
    }

    @Override
    public String toString() {
        return "StatItem{" +
                "time=" + time +
                ", agent='" + agent + '\'' +
                '}';
    }
}
