package io.arusland.storage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class helps to manage difference between client and server time zones.
 * <p>
 * Created by ruslan on 30.12.2016.
 */
public class TimeZoneClientStandard implements TimeZoneClient {
    private final SimpleDateFormat sdfClient = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public synchronized Date fromClient(Date clientTime) {
        try {
            return sdfClient.parse(sdfLocal.format(clientTime));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized Date toClient(Date localTime) {
        try {
            return sdfLocal.parse(sdfClient.format(localTime));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized TimeZone getTimeZone() {
        return sdfClient.getTimeZone();
    }

    public static TimeZoneClientStandard create(TimeZone timeZone) {
        TimeZoneClientStandard result = new TimeZoneClientStandard();
        result.sdfClient.setTimeZone(timeZone != null ? timeZone : TimeZone.getDefault());

        return result;
    }
}
