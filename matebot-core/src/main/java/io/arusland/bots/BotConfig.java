package io.arusland.bots;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Configuration for MateBot.
 * <p>
 * Created by ruslan on 03.12.2016.
 */
public class BotConfig {
    private final Logger log = Logger.getLogger(getClass());
    private final static String CONFIG_PREFIX = "-config=";
    private final Properties prop;
    private final File configFile;

    protected BotConfig(Properties prop, File configFile) {
        this.prop = Validate.notNull(prop, "prop");
        this.configFile = configFile;
    }

    public TimeZone getUserTimeZone(long userId) {
        String tz = getProperty("user" + userId + ".timezone", "");

        if (StringUtils.isNoneBlank(tz)) {
            return TimeZone.getTimeZone(tz);
        }

        return null;
    }

    public void setUserTimeZone(long userId, TimeZone timeZone) {
        if (timeZone != null) {
            prop.setProperty("user" + userId + ".timezone", timeZone.getID());
        } else {
            prop.remove("user" + userId + ".timezone");
        }
    }

    public String getOutputConfigDir() {
        return getProperty("output.config", "");
    }

    public String getMatebotName() {
        return getProperty("matebot.name");
    }

    public String getMatebotToken() {
        return getProperty("matebot.token");
    }

    public long getUserChatId(long userId) {
        String chatIdStr = getProperty("user" + userId + ".chatid", "");

        if (StringUtils.isNoneBlank(chatIdStr)) {
            return Long.parseLong(chatIdStr);
        }

        return 0;
    }

    public void setUserChatId(int userId, long chatId) {
        prop.setProperty("user" + userId + ".chatid", String.valueOf(chatId));
    }

    /**
     * Returns admin user's id.
     *
     * @return Admin User's id.
     */
    public int getAdminId() {
        List<Integer> selectedUsers = getAllowedUsersIds();

        return selectedUsers.isEmpty() ? 0 : selectedUsers.get(0);
    }

    /**
     * Returns allowed users ids.
     */
    public List<Integer> getAllowedUsersIds() {
        if (prop.containsKey("allowed.userids")) {
            String ids = getProperty("allowed.userids");
            try {
                return Arrays.stream(ids.split(","))
                        .filter(p -> !p.isEmpty())
                        .map(p -> Integer.parseInt(p))
                        .collect(Collectors.toList());
            } catch (NumberFormatException ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        return Collections.emptyList();
    }

    public String getMatebotDbRoot() {
        File dir = new File(getProperty("matebot.dbdir"));

        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir.getAbsolutePath();
    }

    public File getConfigFile() {
        return configFile;
    }

    private String getProperty(String key) {
        return Validate.notNull(prop.getProperty(key),
                "Configuration not found for key: " + key);
    }

    private String getProperty(String key, String defValue) {
        String val = prop.getProperty(key);

        return StringUtils.defaultString(val, defValue);
    }

    public void save() {
        OutputStream output = null;

        try {
            output = new FileOutputStream(configFile);
            prop.store(output, null);
        } catch (IOException e) {
            log.error("Save failed", e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static BotConfig load(String fileName) {
        Properties prop = new Properties();
        File file = null;

        try {
            file = new File(fileName).getCanonicalFile();
            try (InputStream input = new FileInputStream(fileName)) {
                prop.load(input);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new BotConfig(prop, file);
    }

    public static BotConfig fromCommandArgs(String[] args) {
        Optional<String> configFile = Arrays.stream(args)
                .filter(p -> p.startsWith(CONFIG_PREFIX))
                .findFirst();

        if (configFile.isPresent()) {
            return BotConfig.load(configFile.get().substring(CONFIG_PREFIX.length()));
        }

        return BotConfig.load("application.properties");
    }

    /**
     * Create instance of {@link BotConfig} in current user home directory.
     */
    public static BotConfig fromUserDir(String configDir) {
        File file;

        if (StringUtils.isNoneBlank(configDir)) {
            file = new File(configDir);
        } else {
            file = new File(System.getProperty("user.home"), ".matebot");
        }

        if (!file.exists()) {
            file.mkdirs();
        }

        file = new File(file, "config.properties");

        if (file.exists()) {
            return BotConfig.load(file.getAbsolutePath());
        }

        Properties prop = new Properties();
        return new BotConfig(prop, file);
    }
}
