package com.example.ortohero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameMap {
    public enum Tile {
        GROUND, WALL, WATER, VILLAGE
    }

    private static final int TILE_SIZE = 48;

    private final Tile[][] tiles;
    private final int width;
    private final int height;
    private final double spawnX;
    private final double spawnY;
    private final List<Obstacle> obstacles;

    public GameMap(Tile[][] tiles, double spawnX, double spawnY, List<Obstacle> obstacles) {
        this.tiles = tiles;
        this.width = tiles.length;
        this.height = tiles[0].length;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.obstacles = new ArrayList<>(obstacles);
    }

    public static GameMap createDefaultMap() {
        String[] rows = new String[]{
                "####################",
                "#....S....#..W....G#",
                "#..####...#..###...#",
                "#..#..#...#....#...#",
                "#..#..#...####.#...#",
                "#..#..#.......#...##",
                "#..#..#####.###...B#",
                "#..#......#...#....#",
                "#..####..M#...#....#",
                "#....T....#...#....#",
                "#P.........#...#..A#",
                "####################"
        };

        int width = rows[0].length();
        int height = rows.length;
        Tile[][] tiles = new Tile[width][height];
        List<Obstacle> obstacles = new ArrayList<>();
        double spawnX = TILE_SIZE * 1.5;
        double spawnY = TILE_SIZE * 9.5;

        for (int y = 0; y < height; y++) {
            String row = rows[y];
            for (int x = 0; x < width; x++) {
                char symbol = row.charAt(x);
                switch (symbol) {
                    case '#':
                        tiles[x][y] = Tile.WALL;
                        break;
                    case 'W':
                        tiles[x][y] = Tile.WATER;
                        break;
                    case 'P':
                        tiles[x][y] = Tile.VILLAGE;
                        spawnX = x * TILE_SIZE + TILE_SIZE / 2.0;
                        spawnY = y * TILE_SIZE + TILE_SIZE / 2.0;
                        break;
                    default:
                        tiles[x][y] = Tile.GROUND;
                        break;
                }

                ObstacleType type = switch (symbol) {
                    case 'S' -> ObstacleType.CREATURE;
                    case 'M' -> ObstacleType.BRIDGE;
                    case 'T' -> ObstacleType.TREE;
                    case 'G' -> ObstacleType.GATE;
                    case 'B' -> ObstacleType.BRIDGE;
                    case 'A' -> ObstacleType.ALTAR;
                    default -> null;
                };
                if (type != null) {
                    obstacles.add(new Obstacle(x, y, type));
                }
            }
        }
        return new GameMap(tiles, spawnX, spawnY, obstacles);
    }

    public Tile getTile(int tileX, int tileY) {
        if (tileX < 0 || tileY < 0 || tileX >= width || tileY >= height) {
            return Tile.WALL;
        }
        return tiles[tileX][tileY];
    }

    public boolean isWalkable(int tileX, int tileY) {
        Tile tile = getTile(tileX, tileY);
        return tile == Tile.GROUND || tile == Tile.VILLAGE;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getSpawnX() {
        return spawnX;
    }

    public double getSpawnY() {
        return spawnY;
    }

    public List<Obstacle> getObstacles() {
        return Collections.unmodifiableList(obstacles);
    }

    public List<Obstacle> mutableObstacles() {
        return obstacles;
    }

    public static int getTileSize() {
        return TILE_SIZE;
    }
}
