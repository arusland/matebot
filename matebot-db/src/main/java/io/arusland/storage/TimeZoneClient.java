package io.arusland.storage;

import java.util.Date;
import java.util.TimeZone;

/**
 * Created by ruslan on 30.12.2016.
 */
public interface TimeZoneClient {
    /**
     * Converts from client time into local time.
     */
    Date fromClient(Date clientTime);

    /**
     * Converts from local time to client time.
     */
    Date toClient(Date localTime);

    /**
     * Format local time as client time.
     */
    String format(Date localTime);

    /**
     * Current time zone.
     */
    TimeZone getTimeZone();
}
