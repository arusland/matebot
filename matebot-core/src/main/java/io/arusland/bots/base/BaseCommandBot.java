package io.arusland.bots.base;

import io.arusland.bots.commands.ItemCommand;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.bots.commands.BotCommand;

import java.util.*;

/**
 * Improved version of {@link TelegramLongPollingCommandBot}.
 * <p>
 * Created by ruslan on 03.12.2016.
 */
public abstract class BaseCommandBot extends TelegramLongPollingBot {
    private final Map<String, BaseBotCommand> commandsMap = new HashMap<>();

    public BaseCommandBot() {
        super();
    }

    protected void register(BaseBotCommand command) {
        commandsMap.put(command.getCommandIdentifier(), command);
    }

    protected void registerAll(List<? extends BaseBotCommand> itemCommands) {
        itemCommands.forEach(p -> register(p));
    }

    @Override
    public final void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.isCommand()) {
                if (executeCommand(update, message)) {
                    return;
                }
            }
        }

        processNonCommandUpdate(update);
    }

    private boolean executeCommand(Update update, Message message) {
        if (message.hasText()) {
            String text = message.getText();
            if (text.startsWith(BotCommand.COMMAND_INIT_CHARACTER)) {
                String commandMessage = text.substring(1);
                String[] commandSplit = commandMessage.split(BotCommand.COMMAND_PARAMETER_SEPARATOR);

                String command = commandSplit[0];

                if (commandsMap.containsKey(command)) {
                    String[] parameters = Arrays.copyOfRange(commandSplit, 1, commandSplit.length);
                    commandsMap.get(command).execute(this, update, parameters);
                    return true;
                }
            }
        }
        return false;
    }

    public List<BaseBotCommand> getRegisteredCommands() {
        return Collections.unmodifiableList(new ArrayList<>(commandsMap.values()));
    }

    protected abstract void processNonCommandUpdate(Update update);
}
