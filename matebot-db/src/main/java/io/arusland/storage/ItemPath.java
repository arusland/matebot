package io.arusland.storage;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Class helper for managing item path. e.g. <code>/audios/music/hello dolly.mp3</code>
 * <p>
 * Created by ruslan on 04.12.2016.
 */
public class ItemPath {
    private final static ItemPath ROOT = new ItemPath(ItemType.ROOT, "/");
    private final static List<String> denied = Arrays.asList("..", ".");

    private final String path;
    private final String shortPath;
    private final ItemType type;

    private ItemPath(ItemType type, String shortPath) {
        this.type = type;
        this.path = makePath(type, shortPath);
        this.shortPath = shortPath;
    }

    public String getPath() {
        return path;
    }

    public ItemType getType() {
        return type;
    }

    public String getShortPath() {
        return shortPath;
    }

    public boolean isRoot() {
        return this == ItemPath.ROOT;
    }

    public String getParentPath() {
        int lastIndex = path.lastIndexOf("/");

        if (lastIndex > 0) {
            return path.substring(0, lastIndex);
        }

        return "/";
    }

    public static ItemPath parse(final String path) {
        if (StringUtils.isBlank(path)) {
            return ItemPath.ROOT;
        }

        List<String> parts = Arrays.stream(path.replace("\\", "/").split("/"))
                .map(p -> p.trim())
                .filter(p -> StringUtils.isNoneBlank(p) && !denied.contains(p))
                .collect(toList());

        if (parts.isEmpty()) {
            return ItemPath.ROOT;
        }

        ItemType itemType = ItemType.valueOf(parts.get(0).toUpperCase());
        parts.remove(0);
        String shortPath = String.join("/", parts);

        return new ItemPath(itemType, shortPath);
    }

    private static String makePath(ItemType type, String shortPath) {
        if (type == ItemType.ROOT) {
            return "/";
        }

        StringBuilder sb = new StringBuilder("/");
        sb.append(type.name().toLowerCase());

        if (!shortPath.isEmpty()) {
            sb.append("/");
            sb.append(shortPath);
        }

        return sb.toString();
    }
}
