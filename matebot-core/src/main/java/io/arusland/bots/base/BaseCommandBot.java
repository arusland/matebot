package io.arusland.bots.base;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.*;

/**
 * Improved version of {@link TelegramLongPollingCommandBot}.
 * <p>
 * Created by ruslan on 03.12.2016.
 */
public abstract class BaseCommandBot extends TelegramLongPollingBot {
    protected final Logger log = Logger.getLogger(getClass());
    protected final static int TEXT_MESSAGE_MAX_LENGTH = 4096;
    private final Map<String, BaseBotCommand> commandsMap = new HashMap<>();

    public BaseCommandBot() {
        super();
    }

    protected abstract BotContext getContext();

    protected void register(BaseBotCommand command) {
        commandsMap.put(command.getCommandIdentifier(), command);
    }

    protected void registerAll(List<? extends BaseBotCommand> itemCommands) {
        itemCommands.forEach(p -> register(p));
    }

    @Override
    public final void onUpdateReceived(Update update) {
        // TODO: handle EditedMessage!
        if (!filter(update.getMessage())) {
            log.info("Received message: " + update.getMessage());

            if (update.hasMessage()) {
                Message message = update.getMessage();
                if (message.isCommand()) {
                    if (executeCommand(update, message)) {
                        return;
                    }
                }
            }

            try {
                processNonCommandUpdate(update);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                sendMessage(update.getMessage().getChatId(), "⚠ error: " + e.getMessage());
            }
        }
    }

    /**
     * <code>true</code> if message must not be handled.
     *
     * @param message incoming message.
     */
    protected abstract boolean filter(Message message);

    private boolean executeCommand(Update update, Message message) {
        BaseBotCommand cmd = getHandlerCommand(message);

        if (cmd != null) {
            boolean isAdmin = getContext().isAdmin(update.getMessage().getFrom().getId());

            if (cmd.isAdminCommand() && !isAdmin) {
                sendMessage(message.getChatId(), "⚠ Sorry, but this command only for admin!");
                return true;
            }

            log.info("Executing command: " + cmd.getCommandIdentifier());
            String[] commandSplit = message.getText().split(BotCommand.COMMAND_PARAMETER_SEPARATOR);
            String[] parameters = Arrays.copyOfRange(commandSplit, 1, commandSplit.length);
            try {
                cmd.execute(this, update, parameters);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                sendMessage(message.getChatId(), "⚠ error: " + e.getMessage());
            }
            return true;
        }

        return false;
    }

    protected BaseBotCommand getHandlerCommand(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            if (text.startsWith(BotCommand.COMMAND_INIT_CHARACTER)) {
                String commandMessage = text.substring(1);
                String[] commandSplit = commandMessage.split(BotCommand.COMMAND_PARAMETER_SEPARATOR);

                String command = commandSplit[0];
                return commandsMap.get(command);
            }
        }

        return null;
    }

    public List<BaseBotCommand> getRegisteredCommands() {
        return Collections.unmodifiableList(new ArrayList<>(commandsMap.values()));
    }

    public void sendMessage(Long chatId, SendMessage sendMessage) {
        try {
            log.info("Sending message: " + sendMessage);

            applyKeyboard(sendMessage);
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendMessage(Long chatId, String message) {
        if (message.length() > TEXT_MESSAGE_MAX_LENGTH) {
            String part1 = message.substring(0, TEXT_MESSAGE_MAX_LENGTH);
            sendMessage(chatId, part1);
            String part2 = message.substring(TEXT_MESSAGE_MAX_LENGTH);
            sendMessage(chatId, part2);
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.enableMarkdown(false);
            sendMessage.setChatId(chatId.toString());
            sendMessage.setText(message);

            sendMessage(chatId, sendMessage);
        }
    }

    private void applyKeyboard(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboad(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("/alerts");
        keyboardFirstRow.add("/notes");
        keyboardFirstRow.add("/ls");
        keyboardFirstRow.add("/up");

        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add("/today");
        keyboardSecondRow.add("/tomorrow");
        keyboardSecondRow.add("/week");
        keyboardSecondRow.add("/month");

        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    protected abstract void processNonCommandUpdate(Update update);
}
