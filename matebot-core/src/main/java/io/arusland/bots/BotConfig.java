package io.arusland.bots;

import org.apache.commons.lang3.Validate;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

/**
 * Configuration for MateBot.
 * <p>
 * Created by ruslan on 03.12.2016.
 */
public class BotConfig {
    private final static String CONFIG_PREFIX = "-config=";
    private final Properties prop;
    private final File configFile;

    protected BotConfig(Properties prop, File configFile) {
        this.prop = Validate.notNull(prop, "prop");
        this.configFile = configFile;
    }

    public String getMatebotName() {
        return getProperty("matebot.name");
    }

    public String getMatebotToken() {
        return getProperty("matebot.token");
    }

    /**
     * If userId defined, only he/she can use this bot.
     *
     * @return User's id.
     */
    public int getSingleUserId() {
        if (prop.containsKey("single.userid")) {
            try {
                return Integer.parseInt(getProperty("single.userid"));
            } catch (NumberFormatException ex) {
            }
        }

        return 0;
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

    public static BotConfig load(String fileName) {
        Properties prop = new Properties();
        File file = null;

        try {
            file = new File(fileName).getCanonicalFile();
            InputStream input = new FileInputStream(fileName);
            prop.load(input);
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
}
