package io.arusland.bots.commands;

import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.List;

/**
 * Command that invoked by recent
 * <p>
 * Created by ruslan on 04.12.2016.
 */
public class ShortcutCommand {
    private final Integer userId;
    private final String shortcut;
    private final String command;
    private final List<String> arguments;

    public ShortcutCommand(Integer userId, String shortcut, String command, String... arguments) {
        this.userId = Validate.notNull(userId, "userId");
        this.shortcut = Validate.notBlank(shortcut, "shortcut");
        this.command = Validate.notNull(command, "command");
        this.arguments = Arrays.asList(arguments);
    }

    public Integer getUserId() {
        return userId;
    }

    public String getShortcut() {
        return shortcut;
    }

    public String getCommand() {
        return command;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public boolean hasArguments() {
        return !arguments.isEmpty();
    }
}
