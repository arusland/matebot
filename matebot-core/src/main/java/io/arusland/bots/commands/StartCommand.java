package io.arusland.bots.commands;

import io.arusland.bots.base.BaseBotCommand;
import io.arusland.bots.base.BotContext;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.AbsSender;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Handle <code>/start</code> command.
 * <p>
 * Created by ruslan on 03.12.2016.
 */
public class StartCommand extends BaseBotCommand implements Comparator<BaseBotCommand> {
    public StartCommand(BotContext context) {
        super("start", "Shows available commands", context);
    }

    @Override
    public void execute(AbsSender absSender, Update update, String[] arguments) {
        StringBuilder sb = new StringBuilder("Hi! You can use next commands:\n");

        List<BaseBotCommand> commands = new ArrayList<>(getContext().getRegisteredCommands());
        commands.sort(this);

        for (BaseBotCommand command : commands) {
            if (!command.equals(this)) {
                sb.append("/");
                sb.append(command.getCommandIdentifier());
                if (StringUtils.isNotBlank(command.getDescription())) {
                    sb.append(" — ");
                    sb.append(command.getDescription());
                }
                sb.append("\n");
            }
        }

        sendMessage(update.getMessage().getChatId(), sb.toString());
    }

    @Override
    public int compare(BaseBotCommand o1, BaseBotCommand o2) {
        return -Integer.compare(o1.getOrder(), o2.getOrder());
    }
}
