package io.arusland.storage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Type of the {@link Item}.
 * <p>
 * Created by ruslan on 03.12.2016.
 */
public enum ItemType {
    ROOT("Root"),

    NOTES("Save notes", "note"),

    ALERTS("Save alerts", "alert"),

    IMAGES("Save images", "jpg", "jpeg", "png", "gif", "tiff"),

    VIDEOS("Save videos", "mp4", "avi", "mkv"),

    AUDIOS("Save audios", "mp3", "wav", "flac", "ogg");

    ItemType(String description, String... extensions) {
        this.description = description;
        this.extensions = Collections.unmodifiableList(Arrays.asList(extensions));
    }

    private final String description;
    private final List<String> extensions;

    public List<String> extensions() {
        return extensions;
    }

    public String description() {
        return description;
    }

    public String normalized() {
        return name().toLowerCase();
    }
}
