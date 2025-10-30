package com.example.ortohero;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TaskSession {
    private final List<TaskWord> words;
    private final TaskManager taskManager;
    private int requiredCorrect;
    private boolean swordUsed = false;
    private boolean armorActive = false;
    private boolean armorConsumed = false;
    private boolean hintUsed = false;

    public TaskSession(List<TaskWord> words, TaskManager taskManager) {
        this.words = new ArrayList<>(words);
        this.taskManager = taskManager;
        this.requiredCorrect = words.size();
    }

    public List<TaskWord> getWords() {
        return words;
    }

    public int getRequiredCorrect() {
        return requiredCorrect;
    }

    public boolean isSwordUsed() {
        return swordUsed;
    }

    public boolean isArmorActive() {
        return armorActive && !armorConsumed;
    }

    public boolean isArmorUsed() {
        return armorActive;
    }

    public boolean useSword() {
        if (swordUsed) {
            return false;
        }
        swordUsed = true;
        requiredCorrect = Math.min(requiredCorrect, Math.min(2, words.size()));
        return true;
    }

    public boolean activateArmor() {
        if (armorActive) {
            return false;
        }
        armorActive = true;
        armorConsumed = false;
        return true;
    }

    public boolean consumeArmorCharge() {
        if (armorActive && !armorConsumed) {
            armorConsumed = true;
            return true;
        }
        return false;
    }

    public boolean useHintAvailable() {
        return !hintUsed;
    }

    public Optional<String> requestHint() {
        if (hintUsed) {
            return Optional.empty();
        }
        for (TaskWord word : words) {
            if (!word.isCorrect()) {
                hintUsed = true;
                return Optional.of(word.getEntry().getHint());
            }
        }
        return Optional.empty();
    }

    public TaskResult evaluate() {
        List<Integer> incorrect = new ArrayList<>();
        int correctCount = 0;
        for (int i = 0; i < words.size(); i++) {
            TaskWord word = words.get(i);
            if (word.isCorrect()) {
                correctCount++;
            } else {
                incorrect.add(i);
            }
        }
        return new TaskResult(correctCount, incorrect);
    }

    public Optional<TaskWord> useWand(TaskWord target) {
        Set<Integer> excluded = new HashSet<>();
        for (TaskWord word : words) {
            excluded.add(word.getEntry().getId());
        }
        Optional<WordEntry> replacement = taskManager.replaceWithEasierWord(target, excluded);
        if (replacement.isPresent()) {
            TaskWord newWord = new TaskWord(replacement.get());
            int index = words.indexOf(target);
            words.set(index, newWord);
            return Optional.of(newWord);
        }
        return Optional.empty();
    }

    public Optional<Character> useFeather(TaskWord word) {
        return word.revealNextLetter();
    }

    public static class TaskResult {
        private final int correctCount;
        private final List<Integer> incorrectIndices;

        public TaskResult(int correctCount, List<Integer> incorrectIndices) {
            this.correctCount = correctCount;
            this.incorrectIndices = incorrectIndices;
        }

        public int getCorrectCount() {
            return correctCount;
        }

        public List<Integer> getIncorrectIndices() {
            return incorrectIndices;
        }

        public boolean isSuccess(int required) {
            return correctCount >= required;
        }
    }
}
