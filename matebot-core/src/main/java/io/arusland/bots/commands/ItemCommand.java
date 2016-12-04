package io.arusland.bots.commands;

import io.arusland.bots.base.BaseBotCommand;
import io.arusland.bots.base.BotContext;
import io.arusland.storage.ItemType;
import org.apache.commons.lang3.Validate;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by ruslan on 03.12.2016.
 */
public class ItemCommand extends BaseBotCommand {
    private final ItemType type;

    public ItemCommand(ItemType type, BotContext context) {
        super(type.name().toLowerCase(), type.description(), context);
        this.type = Validate.notNull(type, "type");
    }

    @Override
    public void execute(AbsSender absSender, Update update, String[] arguments) {
        User user = update.getMessage().getFrom();
        getContext().setCurrentDir(user, "/" + type.normalized());
        getContext().listCurrentDir(update);
    }

    public static List<? extends BaseBotCommand> listAll(BotContext context) {
        return Arrays.stream(ItemType.values())
                .filter(i -> i != ItemType.ROOT)
                .map(i -> new ItemCommand(i, context))
                .collect(toList());
    }
}
