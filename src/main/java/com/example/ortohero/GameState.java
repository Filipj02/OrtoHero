package com.example.ortohero;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class GameState {
    private final WordRepository wordRepository;
    private final GamePersistence persistence;
    private final Random random = new Random();

    private GameMap map;
    private Player player;
    private final PlayerStats playerStats = new PlayerStats();
    private final Inventory inventory = Inventory.starterInventory();
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final TaskManager taskManager;

    public GameState(WordRepository repository, GamePersistence persistence, SaveData saveData) {
        this.wordRepository = repository;
        this.persistence = persistence;
        this.taskManager = new TaskManager(wordRepository, random);
        rebuildWorld();
        if (saveData != null) {
            applySave(saveData);
        }
    }

    private void rebuildWorld() {
        this.map = GameMap.createDefaultMap();
        this.player = new Player(map.getSpawnX(), map.getSpawnY());
        this.obstacles.clear();
        this.obstacles.addAll(map.mutableObstacles());
    }

    private void applySave(SaveData saveData) {
        playerStats.setLevel(saveData.getLevel());
        playerStats.setXp(saveData.getXp());
        playerStats.setMaxLives(saveData.getMaxLives());
        playerStats.setLives(saveData.getLives());
        playerStats.setCorrectWordsTotal(saveData.getCorrectWordsTotal());
        playerStats.setGold(saveData.getGold());
        inventory.clear();
        if (saveData.getInventory() != null) {
            saveData.getInventory().forEach((key, value) -> {
                try {
                    ItemType type = ItemType.valueOf(key);
                    inventory.add(type, value);
                } catch (IllegalArgumentException ignored) {
                }
            });
        }
    }

    public void save() {
        persistence.save(this);
    }

    public void resetProgress() {
        playerStats.reset();
        inventory.clear();
        inventory.add(ItemType.POTION, 1);
        inventory.add(ItemType.FEATHER, 1);
        inventory.add(ItemType.ARMOR, 1);
        rebuildWorld();
    }

    public WordRepository getWordRepository() {
        return wordRepository;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public GameMap getMap() {
        return map;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerStats getPlayerStats() {
        return playerStats;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public Optional<Obstacle> findObstacleNearPlayer(double radius) {
        double playerX = player.getX();
        double playerY = player.getY();
        double tileSize = GameMap.getTileSize();
        return obstacles.stream()
                .filter(obstacle -> !obstacle.isSolved())
                .filter(obstacle -> {
                    double ox = obstacle.getTileX() * tileSize + tileSize / 2.0;
                    double oy = obstacle.getTileY() * tileSize + tileSize / 2.0;
                    double dx = ox - playerX;
                    double dy = oy - playerY;
                    return Math.hypot(dx, dy) <= radius;
                })
                .findFirst();
    }

    public void awardForTask(int correctWords, Runnable onLevelUp) {
        playerStats.addXp(50);
        playerStats.registerCorrectWords(correctWords, level -> {
            if (onLevelUp != null) {
                onLevelUp.run();
            }
            // mała szansa na dodatkowy item przy awansie
            if (random.nextDouble() < 0.25) {
                inventory.add(ItemType.FEATHER, 1);
            }
        });
        int goldEarned = playerStats.randomBonusGoldForLevel();
        playerStats.addGold(goldEarned);
        if (random.nextDouble() < 0.20) {
            inventory.add(randomCommonItem(), 1);
        } else if (random.nextDouble() < 0.05) {
            inventory.add(ItemType.FEATHER, 1);
        }
    }

    private ItemType randomCommonItem() {
        ItemType[] commons = {ItemType.POTION, ItemType.SWORD, ItemType.WAND, ItemType.ARMOR};
        return commons[random.nextInt(commons.length)];
    }

    public void markObstacleSolved(Obstacle obstacle) {
        obstacle.setSolved(true);
    }

    public void removeSolvedObstacles() {
        obstacles.removeIf(Obstacle::isSolved);
    }
}
