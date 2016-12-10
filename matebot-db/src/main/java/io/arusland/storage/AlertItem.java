package io.arusland.storage;

import java.util.Date;

/**
 * <code>AlertItem</code> - special type of item.
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
public interface AlertItem extends Item {
    Date nextTime();

    String getMessage();

    boolean isActive();
}
