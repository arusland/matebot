package io.arusland.storage.file;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * Note format parser and fields holder.
 * <p>
 * Created by ruslan on 24.12.2016.
 */
public class NoteInfo {
    private static final int MAX_TITLE_SIZE = 40;
    public final String content;
    public final String title;

    public NoteInfo(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /**
     * Parses raw string and creates instance of {@link NoteInfo}.
     */
    public static NoteInfo parse(String content) {
        content = StringUtils.defaultString(content, "").trim();
        int indexSemicolon = content.indexOf(';');
        int indexLine = content.indexOf('\n');
        int index = Arrays.asList(indexLine, indexSemicolon).stream()
                .filter(p -> p != -1)
                .min(Integer::compareTo)
                .orElseGet(() -> -1);

        String title = index >= 0 && index < content.length() ? content.substring(0, index) : "";
        title = title.replaceAll("[\r\n]+", "").trim();

        if (StringUtils.isBlank(title)) {
            title = content.substring(0, Math.min(content.length(), MAX_TITLE_SIZE)).trim();
        }

        if (StringUtils.isNoneBlank(title)) {
            if (title.length() > MAX_TITLE_SIZE) {
                title = title.substring(0, MAX_TITLE_SIZE).trim();
            }

            index = title.indexOf(" http");

            if (index > 0) {
                String newTitle = title.substring(0, index).trim();

                if (!newTitle.isEmpty()) {
                    title = newTitle;
                }
            }

            return new NoteInfo(title, content);
        }

        return null;
    }

    @Override
    public String toString() {
        return "NoteInfo{" +
                "content='" + content + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
