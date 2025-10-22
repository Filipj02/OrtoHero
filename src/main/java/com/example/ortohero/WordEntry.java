package com.example.ortohero;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class WordEntry {
    private final int id;
    private final String pattern;
    private final String correct;
    private final String hint;
    private final int difficulty;
    private final String group;

    @JsonCreator
    public WordEntry(
            @JsonProperty("id") int id,
            @JsonProperty("pattern") String pattern,
            @JsonProperty("correct") String correct,
            @JsonProperty("hint") String hint,
            @JsonProperty("difficulty") int difficulty,
            @JsonProperty("group") String group) {
        this.id = id;
        this.pattern = pattern;
        this.correct = correct;
        this.hint = hint;
        this.difficulty = difficulty;
        this.group = group;
    }

    public int getId() {
        return id;
    }

    public String getPattern() {
        return pattern;
    }

    public String getCorrect() {
        return correct;
    }

    public String getHint() {
        return hint;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public String getGroup() {
        return group;
    }
}
