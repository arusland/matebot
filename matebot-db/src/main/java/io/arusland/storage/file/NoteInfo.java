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
        String title = makeTitleByDelimiter(content);

        if (StringUtils.isBlank(title)) {
            title = makeTitleByContent(content);
        }

        if (StringUtils.isNoneBlank(title)) {
            return new NoteInfo(title, content);
        }

        return null;
    }

    private static String makeTitleByContent(String content) {
        String pureContent = content.replaceAll("[\r\n]+", " ").trim();
        String title = pureContent.length() > MAX_TITLE_SIZE ?
            pureContent.substring(0, MAX_TITLE_SIZE).trim() : pureContent;
        int index = pureContent.indexOf(" http");

        if (index > 0 && index < pureContent.length()) {
            title = title.substring(0, index).trim();
        }

        return title;
    }

    private static String makeTitleByDelimiter(String content) {
        int indexSemicolon = content.indexOf(';');
        int indexLine = content.indexOf('\n');
        int index = Arrays.asList(indexLine, indexSemicolon).stream()
                .filter(p -> p != -1)
                .min(Integer::compareTo)
                .orElseGet(() -> -1);

        return index >= 0 && index < content.length()
                ? content.substring(0, index).replaceAll("[\r\n]+", " ").trim()
                : "";
    }

    @Override
    public String toString() {
        return "NoteInfo{" +
                "content='" + content + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
