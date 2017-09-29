package io.arusland.bots;

import io.arusland.bots.base.BaseBotCommand;
import io.arusland.bots.base.BaseCommandBot;
import io.arusland.bots.base.BotContext;
import io.arusland.bots.commands.*;
import io.arusland.bots.utils.AlertsRunner;
import io.arusland.bots.utils.ProcessUtil;
import io.arusland.bots.utils.TimeManagement;
import io.arusland.storage.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.BotSession;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * MateBot - Telegram bot that manages objects like: images, audios, videos, notes and alerts.
 * <p>
 * Created by ruslan on 03.12.2016.
 */
public class MateBot extends BaseCommandBot implements BotContext {
    private final static Logger log = Logger.getLogger(MateBot.class);
    private final BotConfig configInput;
    private final BotConfig configOutput;
    private final Storage storage;
    private final Map<Integer, String> currentPath = new HashMap<>();
    private final List<ShortcutCommand> shortcutCommands = new ArrayList<>();
    private final CommonCommand commonCommand;
    private final AlertsRunner alertsRunner;

    public MateBot(BotConfig configInput) {
        super();
        this.configInput = Validate.notNull(configInput, "configInput");
        this.configOutput = BotConfig.fromUserDir(this.configInput.getOutputConfigDir());
        this.storage = StorageFactory.createStorage(configInput.getMatebotDbRoot(), Collections.emptyMap());
        this.commonCommand = new CommonCommand(this);
        this.alertsRunner = new AlertsRunner(this.storage, new TimeManagement(), new AlertsRunnerHandler(),
                configOutput);
        register(new ListCurrentDirCommand(this));
        register(new StartCommand(this));
        register(new UpDirCommand(this));
        register(new KillBotCommand(this));
        register(new SetTimeZoneCommand(this));
        register(new BackUpCommand(this));
        register(new ShowProcessList(this));
        register(new ShowAlertsToday(this));
        register(new ShowAlertsTomorrow(this));
        register(new ShowAlertsWeek(this));
        register(new ShowAlertsMonth(this));
        registerAll(ItemCommand.listAll(this));

        log.info("MateBot started v0.1");
        log.info("PID - " + ProcessUtil.getCurrentPID());
        log.info("Config file - " + configInput.getConfigFile());
        log.info("Db directory - " + configInput.getMatebotDbRoot());
        ProcessUtil.writePIDSafe(new File("./logs"));
        rerunAlerts();
        sendHelloMessage();
    }

    public static void main(String[] args) {
        start(args);
    }

    public static BotSession start(String[] args) {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

        try {
            BotConfig config = BotConfig.fromCommandArgs(args);
            return telegramBotsApi.registerBot(new MateBot(config));
        } catch (Exception e) {
            log.error("App starting failed", e);
        }

        return null;
    }

    public String getBotUsername() {
        return configInput.getMatebotName();
    }

    @Override
    public String getBotToken() {
        return configInput.getMatebotToken();
    }

    @Override
    protected boolean filter(Message message) {
        List<Integer> selectedUsers = configInput.getAllowedUsersIds();

        if (selectedUsers.isEmpty()) {
            return false;
        }

        int messageUserId = message.getFrom().getId();
        boolean userIsOK = selectedUsers.contains(messageUserId);

        if (!userIsOK) {
            log.warn("Message from alien skipped: " + message);
            sendMessage(message.getChatId(),
                    "\uD83E\uDD16 Sorry, but it is personal bot. You can install you own one from https://github.com/arusland/matebot");
            return true;
        }

        if (configOutput.getUserTimeZone(message.getFrom().getId()) == null) {
            BaseBotCommand cmd = getHandlerCommand(message);

            if (!(cmd instanceof SetTimeZoneCommand)) {
                sendMessage(message.getChatId(),
                        "⚠ Sorry, but you need to set up your time zone. Via command: /tz +5:00");
                return true;
            }
        }

        // save user's private chat's id
        saveUserPrivateChatId(message);

        return false;
    }

    private void saveUserPrivateChatId(Message message) {
        Integer messageUserId = message.getFrom().getId();
        Long chatId = message.getChatId();
        long savedChatId = configOutput.getUserChatId(messageUserId);

        if (chatId != null && chatId > 0
                && savedChatId != chatId
                && message.getChat().isUserChat()) {
            configOutput.setUserChatId(messageUserId, chatId);
            configOutput.save();
        }
    }

    @Override
    protected void processNonCommandUpdate(Update update) {
        commonCommand.execute(this, update, new String[0]);
    }

    @Override
    public UserStorage getUserStorage(User user) {
        io.arusland.storage.User user2 = new io.arusland.storage.User(user.getId(), user.getUserName());
        UserStorage userStorage = storage.getOrCreate(user2);
        userStorage.setTimeZone(getTimeZone(user));

        return userStorage;
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
    public void rerunAlerts() {
        alertsRunner.rerunAlerts();
    }

    @Override
    public Date fromClient(User user, Date clientTime) {
        return getTimeZoneClient(user.getId()).fromClient(clientTime);
    }

    @Override
    public Date toClient(User user, Date clientTime) {
        return getTimeZoneClient(user.getId()).toClient(clientTime);
    }

    @Override
    public void setTimeZone(User user, TimeZone timeZone) {
        configOutput.setUserTimeZone(user.getId(), timeZone);
        configOutput.save();
    }

    @Override
    public TimeZone getTimeZone(User user) {
        return configOutput.getUserTimeZone(user.getId());
    }

    @Override
    public List<AlertItem> nextAlerts(Date dateTo) {
        return alertsRunner.nextAlerts(dateTo);
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
    public void addShortcutCommand(Integer userId, String shortcut, String cmd, String... args) {
        ShortcutCommand shcmd = getShortcutCommand(userId, shortcut);

        if (shcmd != null) {
            shortcutCommands.remove(shcmd);
        }

        shortcutCommands.add(new ShortcutCommand(userId, shortcut, cmd, args));
    }

    @Override
    public ShortcutCommand getShortcutCommand(Integer userId, String shortcut) {
        return shortcutCommands.stream()
                .filter(p -> p.getUserId().equals(userId) && p.getShortcut().equals(shortcut))
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

    private class AlertsRunnerHandler implements BiConsumer<AlertItem, Long> {
        @Override
        public void accept(AlertItem alertItem, Long userId) {
            long chatId = configOutput.getUserChatId(userId);

            if (chatId > 0) {
                StringBuilder sb = new StringBuilder();
                String removeFile = "/remove";

                if (StringUtils.isNoneBlank(alertItem.getMessage())) {
                    sb.append("ALERT: " + alertItem.getMessage());
                } else {
                    sb.append("ALERT!!!");
                }

                sb.append("\n\n❌");
                sb.append(removeFile);
                sb.append("\n");

                addShortcutCommand((int) ((long) userId), removeFile, "rm", alertItem.getFullPath(), "0");


                sendMessage(chatId, sb.toString());
            }
        }
    }

    private TimeZoneClient getTimeZoneClient(long userId) {
        TimeZone timeZone = configOutput.getUserTimeZone(userId);

        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        return TimeZoneClientStandard.create(timeZone);
    }

    private void sendHelloMessage() {
        configInput.getAllowedUsersIds().forEach(userId -> {
            if (userId > 0) {
                TimeZoneClient client = getTimeZoneClient(userId);
                sendMessage((long) userId, "Matebot started at " + client.format(new Date()));
            }
        });
    }
}
