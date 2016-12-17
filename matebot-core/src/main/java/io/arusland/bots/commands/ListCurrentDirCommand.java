package io.arusland.bots.commands;

import io.arusland.bots.base.BaseBotCommand;
import io.arusland.bots.base.BotContext;
import io.arusland.storage.AlertItem;
import io.arusland.storage.Item;
import io.arusland.storage.UserStorage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import java.util.List;

/**
 * List items of current directory. If current directory not defined,
 * show root directories.
 * <p>
 * Created by ruslan on 04.12.2016.
 */
public class ListCurrentDirCommand extends BaseBotCommand {
    private static String EMOJI_DIR = "\uD83D\uDCC1";
    private static String EMOJI_FILE = "\uD83D\uDDC4";

    public ListCurrentDirCommand(BotContext context) {
        super("ls", "List items of current directory!", context);
        setOrder(30);
    }

    @Override
    public void execute(AbsSender absSender, Update update, String[] arguments) {
        User user = update.getMessage().getFrom();
        UserStorage storage = getContext().getUserStorage(user);
        Message msg = update.getMessage();
        String currentPath = getContext().getCurrentPath(user);

        if (StringUtils.isBlank(currentPath)) {
            getContext().showRoots(update);
        } else {
            Item currentItem = storage.getItemByPath(currentPath);

            if (currentItem == null) {
                getContext().setCurrentDir(user, "");
                getContext().showRoots(update);
            } else {
                StringBuilder sb = new StringBuilder();

                sb.append("Current: ");
                sb.append(currentItem.getFullPath());
                sb.append("\n");

                if (currentItem.isDirectory()) {
                    String parentPath = currentItem.getParentPath();

                    if (StringUtils.isNotBlank(parentPath)) {
                        sb.append("/up");
                        sb.append("\n");
                    }

                    List<Item> items = currentItem.listItems();

                    for (int i = 0; i < items.size(); i++) {
                        Item item = items.get(i);

                        if (item instanceof AlertItem) {
                            renderAlertItem(user, sb, i, (AlertItem) item);
                        } else {
                            renderCommonItem(user, sb, i, item);
                        }
                    }
                }

                sendMessage(msg.getChat().getId(), sb.toString());
            }
        }
    }

    private void renderAlertItem(User user, StringBuilder sb, int index, AlertItem item) {
        String changeDirShortcut = "/" + (index + 1);
        String removeFileShortcut = "/del" + (index + 1);
        sb.append(changeDirShortcut);
        sb.append(" ");

        if (item.isDirectory()) {
            sb.append(EMOJI_DIR);
        } else {
            if (item.isActive()) {
                sb.append("\uD83D\uDD14");
            } else {
                sb.append("❌");
            }
        }
        sb.append(StringUtils.defaultString(item.getTitle(), item.getName()));
        sb.append("\n");

        if (item.isDirectory()) {
            getContext().addShortcutCommand(user, changeDirShortcut, "cd", item.getFullPath());
        } else {
            getContext().addShortcutCommand(user, changeDirShortcut, "dl", item.getFullPath());
            getContext().addShortcutCommand(user, removeFileShortcut, "rm", item.getFullPath());
        }
    }

    public void renderCommonItem(User user, StringBuilder sb, int index, Item item) {
        String changeDirShortcut = "/" + (index + 1);
        String removeFileShortcut = "/del" + (index + 1);
        sb.append(changeDirShortcut);
        sb.append(" ");

        if (item.isDirectory()) {
            sb.append(EMOJI_DIR);
        } else {
            sb.append(EMOJI_FILE);
        }
        sb.append(StringUtils.isNoneBlank(item.getTitle()) ? item.getTitle() : item.getName());
        if (!item.isDirectory()) {
            sb.append(" - ");
            sb.append(FileUtils.byteCountToDisplaySize(item.getSize()));
            sb.append(" ");
            sb.append(removeFileShortcut);
        }

        sb.append("\n");

        if (item.isDirectory()) {
            getContext().addShortcutCommand(user, changeDirShortcut, "cd", item.getFullPath());
        } else {
            getContext().addShortcutCommand(user, changeDirShortcut, "dl", item.getFullPath());
            getContext().addShortcutCommand(user, removeFileShortcut, "rm", item.getFullPath());
        }
    }
}
