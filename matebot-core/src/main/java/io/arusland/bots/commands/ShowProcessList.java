package io.arusland.bots.commands;

import io.arusland.bots.base.BaseBotCommand;
import io.arusland.bots.base.BotContext;
import io.arusland.bots.utils.ExecUtils;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.AbsSender;

import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 *
 * Show java processes list.
 *
 * @author Ruslan Absalyamov
 * @since 2017-04-03
 */
public class ShowProcessList extends BaseBotCommand {
    public ShowProcessList(BotContext context) {
        super("ps", "Show java processes list", context);
    }

    @Override
    public void execute(AbsSender absSender, Update update, String[] arguments) {
        long chatId = update.getMessage().getChat().getId();

        try {
            String pid = getPid();
            String content = "PID - " + pid + "\n\n";
            String output = ExecUtils.runCommand("jps");
            sendMessage(chatId, content + output);
        } catch (Exception e) {
            sendMessage(chatId, "EXCEPTION: " +e.getMessage());
        }
    }

    private String getPid() {
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        int index  = pid.indexOf('@');

        if (index > 0) {
            pid = pid.substring(0, index);
        }

        return pid;
    }

    @Override
    public boolean isVisible() {
        return false;
    }
}
