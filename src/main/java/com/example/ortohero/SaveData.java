package com.example.ortohero;

import java.util.Map;

public class SaveData {
    private int level;
    private int xp;
    private int lives;
    private int maxLives;
    private int correctWordsTotal;
    private int gold;
    private Map<String, Integer> inventory;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getMaxLives() {
        return maxLives;
    }

    public void setMaxLives(int maxLives) {
        this.maxLives = maxLives;
    }

    public int getCorrectWordsTotal() {
        return correctWordsTotal;
    }

    public void setCorrectWordsTotal(int correctWordsTotal) {
        this.correctWordsTotal = correctWordsTotal;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public Map<String, Integer> getInventory() {
        return inventory;
    }

    public void setInventory(Map<String, Integer> inventory) {
        this.inventory = inventory;
    }
}
