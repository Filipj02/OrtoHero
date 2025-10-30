package com.example.ortohero;

import java.util.Random;

public class PlayerStats {
    private static final int XP_THRESHOLD = 100;
    private final Random random = new Random();

    private int level = 1;
    private int xp = 0;
    private int lives = 3;
    private int maxLives = 3;
    private int correctWordsTotal = 0;
    private int gold = 0;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = Math.max(0, xp);
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = Math.max(0, Math.min(lives, maxLives));
    }

    public int getMaxLives() {
        return maxLives;
    }

    public void setMaxLives(int maxLives) {
        this.maxLives = Math.max(1, maxLives);
        if (lives > this.maxLives) {
            lives = this.maxLives;
        }
    }

    public int getCorrectWordsTotal() {
        return correctWordsTotal;
    }

    public void setCorrectWordsTotal(int correctWordsTotal) {
        this.correctWordsTotal = Math.max(0, correctWordsTotal);
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = Math.max(0, gold);
    }

    public void addXp(int amount) {
        xp += Math.max(0, amount);
        while (xp >= XP_THRESHOLD) {
            xp -= XP_THRESHOLD;
            onLevelUpXpBonus();
        }
    }

    private void onLevelUpXpBonus() {
        // XP bar loops, but we do not level up here; XP bonus is handled separately
    }

    public void addGold(int amount) {
        gold = Math.max(0, gold + amount);
    }

    public double getXpProgress() {
        return Math.min(1.0, Math.max(0.0, xp / (double) XP_THRESHOLD));
    }

    public void loseLife() {
        if (lives > 0) {
            lives--;
        }
    }

    public void gainLife() {
        if (lives < maxLives) {
            lives++;
        }
    }

    public boolean isAlive() {
        return lives > 0;
    }

    public void reset() {
        level = 1;
        xp = 0;
        lives = 3;
        maxLives = 3;
        correctWordsTotal = 0;
        gold = 0;
    }

    public void registerCorrectWords(int count, LevelUpListener listener) {
        for (int i = 0; i < count; i++) {
            correctWordsTotal++;
            while (correctWordsTotal >= thresholdForLevel(level + 1)) {
                level++;
                maxLives++;
                lives = maxLives;
                xp = Math.min(xp + 25, XP_THRESHOLD);
                if (listener != null) {
                    listener.onLevelUp(level);
                }
            }
        }
    }

    private int thresholdForLevel(int targetLevel) {
        if (targetLevel <= 1) {
            return 0;
        }
        if (targetLevel == 2) {
            return 5;
        }
        return 5 + 3 * (targetLevel - 2);
    }

    public int randomBonusGoldForLevel() {
        return switch (level) {
            case 1, 2 -> random.nextInt(5, 11);
            case 3, 4 -> random.nextInt(10, 16);
            default -> random.nextInt(12, 22);
        };
    }

    public interface LevelUpListener {
        void onLevelUp(int newLevel);
    }
}
