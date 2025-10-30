package com.example.ortohero;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class TaskWord {
    private final WordEntry entry;
    private final StringProperty answer = new SimpleStringProperty("");
    private final Set<Integer> revealedIndices = new HashSet<>();

    public TaskWord(WordEntry entry) {
        this.entry = entry;
    }

    public WordEntry getEntry() {
        return entry;
    }

    public StringProperty answerProperty() {
        return answer;
    }

    public String getAnswer() {
        return answer.get();
    }

    public void setAnswer(String value) {
        answer.set(value);
    }

    public boolean isCorrect() {
        return sanitize(answer.get()).equals(sanitize(entry.getCorrect()));
    }

    private String sanitize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "").toLowerCase();
    }

    public Optional<Character> revealNextLetter() {
        String pattern = entry.getPattern();
        String correct = entry.getCorrect();
        for (int i = 0; i < Math.min(pattern.length(), correct.length()); i++) {
            if (pattern.charAt(i) == '.' && !revealedIndices.contains(i)) {
                revealedIndices.add(i);
                char letter = correct.charAt(i);
                applyReveal(i, letter);
                return Optional.of(letter);
            }
        }
        return Optional.empty();
    }

    private void applyReveal(int index, char letter) {
        String current = answer.get() == null ? "" : answer.get();
        char[] buffer = new char[Math.max(current.length(), index + 1)];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = i < current.length() ? current.charAt(i) : ' ';
        }
        buffer[index] = letter;
        answer.set(String.valueOf(buffer).stripTrailing());
    }
}
