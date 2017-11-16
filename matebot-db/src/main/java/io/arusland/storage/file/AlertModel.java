package io.arusland.storage.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Represents json model for .alert-file format.
 *
 * @author Ruslan Absalyamov
 * @since 2017-11-16
 */
public class AlertModel {
    private String input;
    private Long lastActivePeriodTime;

    public AlertModel() {
    }

    public AlertModel(String input, Date lastActivePeriodTime) {
        this(input, lastActivePeriodTime != null ? lastActivePeriodTime.getTime() : null);
    }

    public AlertModel(String input, Long lastActivePeriodTime) {
        this.input = input;
        this.lastActivePeriodTime = lastActivePeriodTime;
    }

    public String getInput() {
        return StringUtils.defaultString(input);
    }

    public void setInput(String input) {
        this.input = input;
    }

    public Date getLastActivePeriodTimeDate() {
        return lastActivePeriodTime != null ? new Date(lastActivePeriodTime) : null;
    }

    public Long getLastActivePeriodTime() {
        return lastActivePeriodTime;
    }

    public void setLastActivePeriodTime(Long lastActivePeriodTime) {
        this.lastActivePeriodTime = lastActivePeriodTime;
    }

    public static AlertModel parseFile(String content) {
        try {
            AlertModel model = new Gson().fromJson(content, AlertModel.class);

            if (model != null) {
                return model;
            }
        } catch (JsonSyntaxException e) {
            // ignore this. we must support old format which simple plain format
        }

        return new AlertModel(content, (Long)null);
    }

    public String toFileString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

    @Override
    public String toString() {
        return "AlertModel{" +
                "input='" + input + '\'' +
                ", lastActivePeriodTime=" + lastActivePeriodTime +
                '}';
    }
}
