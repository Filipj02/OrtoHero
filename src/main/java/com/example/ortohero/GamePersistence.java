package com.example.ortohero;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GamePersistence {
    private final ObjectMapper mapper;
    private final Path savePath;

    public GamePersistence() {
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        Path directory = Path.of(System.getProperty("user.home"), ".ortohero");
        this.savePath = directory.resolve("save.json");
        try {
            Files.createDirectories(directory);
        } catch (IOException ignored) {
        }
    }

    public Optional<SaveData> load() {
        if (!Files.exists(savePath)) {
            return Optional.empty();
        }
        try {
            byte[] bytes = Files.readAllBytes(savePath);
            if (bytes.length == 0) {
                return Optional.empty();
            }
            return Optional.of(mapper.readValue(bytes, SaveData.class));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public void save(GameState state) {
        SaveData data = new SaveData();
        PlayerStats stats = state.getPlayerStats();
        data.setLevel(stats.getLevel());
        data.setXp(stats.getXp());
        data.setLives(stats.getLives());
        data.setMaxLives(stats.getMaxLives());
        data.setCorrectWordsTotal(stats.getCorrectWordsTotal());
        data.setGold(stats.getGold());
        Map<String, Integer> inventoryMap = new HashMap<>();
        state.getInventory().getItems().forEach((type, qty) -> inventoryMap.put(type.name(), qty));
        data.setInventory(inventoryMap);
        try {
            mapper.writeValue(savePath.toFile(), data);
        } catch (IOException ignored) {
        }
    }
}
