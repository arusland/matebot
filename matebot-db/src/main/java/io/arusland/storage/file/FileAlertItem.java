package io.arusland.storage.file;

import io.arusland.storage.AlertItem;
import io.arusland.storage.ItemPath;
import io.arusland.storage.ItemType;
import io.arusland.storage.User;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ruslan on 10.12.2016.
 */
public class FileAlertItem extends FileItem implements AlertItem {
    private static final Pattern ALERT_FULL_PATTERN = Pattern.compile("^(\\d\\d*):(\\d\\d*) (\\d\\d*):(\\d\\d*):(\\d{4})( .*)*$");
    private static final Pattern ALERT_FULL_PATTERN2 = Pattern.compile("^(\\d\\d*):(\\d\\d*) (\\d\\d*):(\\d\\d*)( .*)*$");
    private static final Pattern ALERT_FULL_PATTERN3 = Pattern.compile("^(\\d\\d*):(\\d\\d*) (\\d\\d*):( .*)*$");
    private static final Pattern ALERT_SHORT_PATTERN = Pattern.compile("^(\\d\\d*):(\\d\\d*)( .*)*$");
    private static final Pattern ALERT_WEEK_PATTERN = Pattern.compile("^(\\d\\d*):(\\d\\d*) ([\\d\\-,]+)( .*)*$");
    private static final Pattern WEEK_DAY_PATTERN = Pattern.compile("^[1-7]$");
    private static final Pattern WEEK_DAY_PATTERN2 = Pattern.compile("^([1-7])-([1-7])$");

    public FileAlertItem(User user, ItemType type, File file, ItemPath path) {
        super(user, type, file, path);
    }

    @Override
    public Date nextTime() {
        return null;
    }

    @Override
    public String getMessage() {
        return null;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    public static AlertInfo parse(final String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }

        Matcher mc = ALERT_FULL_PATTERN.matcher(input);

        if (mc.find()) {
            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)), Integer.parseInt(mc.group(3)),
                    Integer.parseInt(mc.group(4)), Integer.parseInt(mc.group(5)), mc.group(6));
        }

        mc = ALERT_FULL_PATTERN2.matcher(input);

        if (mc.find()) {
            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)), Integer.parseInt(mc.group(3)),
                    Integer.parseInt(mc.group(4)), null, mc.group(5));
        }

        mc = ALERT_FULL_PATTERN3.matcher(input);

        if (mc.find()) {
            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)), Integer.parseInt(mc.group(3)),
                    null, null, mc.group(4));
        }

        mc = ALERT_WEEK_PATTERN.matcher(input);

        if (mc.find()) {
            int flags = parseFlags(mc.group(3));

            if (flags > 0) {
                return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)), flags, mc.group(4));
            }
        }

        mc = ALERT_SHORT_PATTERN.matcher(input);

        if (mc.find()) {
            return new AlertInfo(Integer.parseInt(mc.group(1)), Integer.parseInt(mc.group(2)), mc.group(3));
        }

        return null;
    }

    /**
     * Convert week days to flags representation
     * <p>
     * 6   => 00100000b<br>
     * 1-5 => 00011111b<br>
     * 1,6-7 => 01100001b<br>
     */
    private static int parseFlags(String input) {
        List<Boolean> errors = new ArrayList<>();
        List<Integer> flags = new ArrayList<>();

        Arrays.stream(input.split(",")).forEach(s -> {
            if (WEEK_DAY_PATTERN.matcher(s).find()) {
                flags.add(1 << (Integer.parseInt(s) - 1));
            } else {
                Matcher mc = WEEK_DAY_PATTERN2.matcher(s);

                if (mc.find()) {
                    int start = Integer.parseInt(mc.group(1));
                    int end = Integer.parseInt(mc.group(2));

                    if (start <= end) {
                        for (int i = start; i <= end; i++) {
                            flags.add(1 << (i - 1));
                        }
                    } else {
                        errors.add(true);
                    }
                } else {
                    errors.add(true);
                }
            }
        });

        if (errors.isEmpty()) {
            return flags.stream().reduce(0, (flg, next) -> flg |= next);
        }

        return 0;
    }
}
