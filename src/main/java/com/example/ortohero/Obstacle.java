package com.example.ortohero;

public class Obstacle {
    private final int tileX;
    private final int tileY;
    private final ObstacleType type;
    private boolean solved;

    public Obstacle(int tileX, int tileY, ObstacleType type) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.type = type;
        this.solved = false;
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public ObstacleType getType() {
        return type;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }
}
