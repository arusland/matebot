package io.arusland.storage;

import java.util.Date;
import java.util.List;

/**
 * <code>AlertItem</code> - special type of {@link Item}.
 * <p>
 * Store information about alert time and text message.
 * <p>
 * Formats:<br/>
 * hh:mm<br/>
 * hh:mm Message<br/>
 * hh:mm 1-5,7          // show alert at hh:mm every working day and Sunday<br/>
 * hh:mm 1-5,7 Message<br/>
 * hh:mm dd:mm:yyyy<br/>
 * hh:mm dd:mm:yyyy Message<br/>
 * hh:mm dd:mm<br/>
 * hh:mm dd:
 * <p>
 * Created by ruslan on 10.12.2016.
 */
public interface AlertItem extends Item<AlertItem> {
    /**
     * Next alert time. Local time zone.
     */
    Date nextTime();

    /**
     * Alert message.
     */
    String getMessage();

    String getMessageShort();

    /**
     * Alert raw source.
     */
    String getSource();

    /**
     * Alert can be scheduled.
     */
    boolean isActive();

    /**
     * Returns true if alert has active period.
     */
    boolean isPeriodActive();

    /**
     * Cancel active period and it will be recalculated after next alert time.
     */
    void cancelActivePeriod();
}
