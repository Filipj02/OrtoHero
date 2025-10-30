package com.example.ortohero;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public class TaskManager {
    private final WordRepository repository;
    private final Random random;

    public TaskManager(WordRepository repository, Random random) {
        this.repository = repository;
        this.random = random;
    }

    public TaskSession createSession(PlayerStats stats) {
        int wordsCount = 5;
        List<TaskWord> words = new ArrayList<>();
        Set<Integer> usedIds = new HashSet<>();

        for (int i = 0; i < wordsCount; i++) {
            int difficulty = pickDifficulty(stats.getLevel());
            Optional<WordEntry> entry = repository.randomWord(difficulty, random, usedIds);
            if (entry.isEmpty()) {
                // fallback - pick any available word
                entry = repository.getEntries().stream()
                        .filter(e -> !usedIds.contains(e.getId()))
                        .findAny();
            }
            if (entry.isPresent()) {
                usedIds.add(entry.get().getId());
                words.add(new TaskWord(entry.get()));
            }
        }
        return new TaskSession(words, this);
    }

    private int pickDifficulty(int level) {
        double easyWeight = level >= 5 ? 0.4 : 0.6;
        double mediumWeight = level >= 5 ? 0.4 : 0.3;
        double hardWeight = level >= 5 ? 0.2 : 0.1;
        double roll = random.nextDouble();
        if (roll < easyWeight) {
            return 1;
        }
        if (roll < easyWeight + mediumWeight) {
            return 2;
        }
        return 3;
    }

    Optional<WordEntry> replaceWithEasierWord(TaskWord taskWord, Set<Integer> excluded) {
        int currentDifficulty = taskWord.getEntry().getDifficulty();
        int targetDifficulty = Math.max(1, currentDifficulty - 1);
        return repository.randomWord(taskWord.getEntry().getGroup(), targetDifficulty, random, excluded);
    }
}
