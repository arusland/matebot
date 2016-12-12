package io.arusland.bots;

import io.arusland.bots.base.BaseCommandBot;
import io.arusland.bots.base.BotContext;
import io.arusland.bots.commands.*;
import io.arusland.bots.utils.TimeManagement;
import io.arusland.storage.AlertItem;
import io.arusland.storage.Storage;
import io.arusland.storage.StorageFactory;
import io.arusland.storage.UserStorage;
import org.apache.commons.lang3.Validate;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.File;
import java.util.*;

/**
 * MateBot - Telegram bot that manages objects like: images, audios, videos, notes and alerts.
 * <p>
 * Created by ruslan on 03.12.2016.
 */
public class MateBot extends BaseCommandBot implements BotContext {
    private final BotConfig config;
    private final Storage storage;
    private final Map<Integer, String> currentPath = new HashMap<>();
    private final List<ShortcutCommand> shortcutCommands = new ArrayList<>();
    private final CommonCommand commonCommand;
    private final TimeManagement timeManagement = new TimeManagement();
    private final Map<AlertItem, Date> alerts = Collections.synchronizedMap(new IdentityHashMap<>());

    public MateBot(BotConfig config) {
        super();
        this.config = Validate.notNull(config, "config");
        this.storage = StorageFactory.createStorage(config.getMatebotDbRoot(), Collections.emptyMap());
        this.commonCommand = new CommonCommand(this);
        register(new ListCurrentDirCommand(this));
        register(new StartCommand(this));
        register(new UpDirCommand(this));
        registerAll(ItemCommand.listAll(this));

        log.info("MateBot started v0.1");
        log.info("Config file - " + config.getConfigFile());
        log.info("Db directory - " + config.getMatebotDbRoot());
    }

    public static void main(String[] args) {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            BotConfig config = BotConfig.fromCommandArgs(args);
            telegramBotsApi.registerBot(new MateBot(config));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {
        return config.getMatebotName();
    }

    @Override
    public String getBotToken() {
        return config.getMatebotToken();
    }

    @Override
    protected boolean filter(Message message) {
        int userId = config.getSingleUserId();

        if (userId == 0) {
            return false;
        }

        return message.getFrom().getId() != userId;
    }

    @Override
    protected void processNonCommandUpdate(Update update) {
        commonCommand.execute(this, update, new String[0]);
    }

    @Override
    public UserStorage getUserStorage(User user) {
        io.arusland.storage.User user2 = new io.arusland.storage.User(user.getId(), user.getUserName());
        return storage.getOrCreate(user2);
    }

    @Override
    public void setCurrentDir(User user, String path) {
        currentPath.put(user.getId(), path);
        clearShortcutCommands(user);
    }

    @Override
    public void clearShortcutCommands(User user) {
        shortcutCommands.removeIf(p -> p.getUserId().equals(user.getId()));
    }

    @Override
    public void enqueueAlert(AlertItem alert, Runnable handler) {
        Date time = alert.nextTime();

        if (time != null) {
            alerts.put(alert, time);
            timeManagement.enqueue(time, () -> {
                alerts.remove(time);
                handler.run();
            });
        }
    }

    @Override
    public void dequeueAlert(AlertItem alert) {
        Date time = alerts.get(alert);

        if (time != null) {
            alerts.remove(alert);
            timeManagement.dequeue(time);
        }
    }

    @Override
    public String getCurrentPath(User user) {
        if (currentPath.containsKey(user.getId())) {
            return currentPath.get(user.getId());
        }

        return "";
    }

    @Override
    public void listCurrentDir(Update update) {
        getRegisteredCommands().stream()
                .filter(p -> p instanceof ListCurrentDirCommand)
                .findFirst().get()
                .execute(this, update, new String[0]);
    }

    @Override
    public void showRoots(Update update) {
        getRegisteredCommands().stream()
                .filter(p -> p instanceof StartCommand)
                .findFirst().get()
                .execute(this, update, new String[0]);
        clearShortcutCommands(update.getMessage().getFrom());
    }

    @Override
    public void addShortcutCommand(User user, String shortcut, String cmd, String... args) {
        ShortcutCommand shcmd = getShortcutCommand(user, shortcut);

        if (shcmd != null) {
            shortcutCommands.remove(shcmd);
        }

        shortcutCommands.add(new ShortcutCommand(user.getId(), shortcut, cmd, args));
    }

    @Override
    public ShortcutCommand getShortcutCommand(User user, String shortcut) {
        return shortcutCommands.stream()
                .filter(p -> p.getUserId().equals(user.getId()) && p.getShortcut().equals(shortcut))
                .findFirst()
                .orElseGet(() -> null);
    }

    @Override
    public void sendFile(Long chatId, File file) {
        SendDocument doc = new SendDocument();
        doc.setChatId(chatId.toString());
        doc.setNewDocument(file);

        try {
            log.info("Sending file: " + doc);
            sendDocument(doc);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public org.telegram.telegrambots.api.objects.File doGetFile(GetFile getFile) {
        try {
            return getFile(getFile);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public File doDownloadFile(org.telegram.telegrambots.api.objects.File file) {
        try {
            return downloadFile(file);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
