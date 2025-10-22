package com.example.ortohero;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class WordRepository {
    private final List<WordEntry> entries = new ArrayList<>();
    private final Map<Integer, List<WordEntry>> byDifficulty = new HashMap<>();
    private final Map<String, List<WordEntry>> byGroup = new HashMap<>();

    public WordRepository() throws IOException {
        load();
    }

    private void load() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream inputStream = WordRepository.class.getResourceAsStream("/com/example/ortohero/data/words.json")) {
            if (inputStream == null) {
                throw new IOException("Nie znaleziono pliku words.json");
            }
            CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, WordEntry.class);
            List<WordEntry> loaded = mapper.readValue(inputStream, listType);
            entries.clear();
            entries.addAll(loaded);
            byDifficulty.clear();
            byGroup.clear();
            for (WordEntry entry : entries) {
                byDifficulty.computeIfAbsent(entry.getDifficulty(), key -> new ArrayList<>()).add(entry);
                byGroup.computeIfAbsent(entry.getGroup(), key -> new ArrayList<>()).add(entry);
            }
        }
    }

    public List<WordEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public List<WordEntry> getByDifficulty(int difficulty) {
        return byDifficulty.getOrDefault(difficulty, List.of());
    }

    public Optional<WordEntry> randomWord(int difficulty, Random random, Set<Integer> excludedIds) {
        List<WordEntry> pool = getByDifficulty(difficulty).stream()
                .filter(entry -> excludedIds == null || !excludedIds.contains(entry.getId()))
                .collect(Collectors.toList());
        if (pool.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(pool.get(random.nextInt(pool.size())));
    }

    public Optional<WordEntry> randomWord(String group, int maxDifficulty, Random random, Set<Integer> excludedIds) {
        List<WordEntry> pool = byGroup.getOrDefault(group, List.of()).stream()
                .filter(entry -> entry.getDifficulty() <= maxDifficulty)
                .filter(entry -> excludedIds == null || !excludedIds.contains(entry.getId()))
                .collect(Collectors.toList());
        if (pool.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(pool.get(random.nextInt(pool.size())));
    }
}
