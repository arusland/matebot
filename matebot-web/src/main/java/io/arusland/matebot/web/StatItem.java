package io.arusland.matebot.web;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Ruslan Absalyamov
 * @since 2017-04-25
 */
public class StatItem {
    private final static String SDF = "yyyy-MM-dd HH:mm:ss";
    private final Date time;
    private final String agent;
    private final String ip;

    public StatItem(Date time, String ip, String agent) {
        this.time = (Date)time.clone();
        this.ip = ip;
        this.agent = agent;
    }

    public Date getTime() {
        return time;
    }

    public String getAgent() {
        return agent;
    }

    public String getIp() {
        return ip;
    }

    public String formatTime() {
        return new SimpleDateFormat(SDF).format(time);
    }

    @Override
    public String toString() {
        return "StatItem{" +
                "time=" + time +
                ", agent='" + agent + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
