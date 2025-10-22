package com.example.ortohero;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class GameView extends StackPane {
    private static final String DEFAULT_MESSAGE = "Strzałki – ruch | Spacja – interakcja | I – ekwipunek | Esc – pauza";

    private final GameState state;
    private final Canvas canvas;
    private final HUDOverlay hudOverlay = new HUDOverlay();
    private final InventoryOverlay inventoryOverlay = new InventoryOverlay();
    private final TaskDialog taskDialog = new TaskDialog();
    private final PauseOverlay pauseOverlay = new PauseOverlay();
    private final Label messageLabel = new Label(DEFAULT_MESSAGE);

    private final Set<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class);
    private final AnimationTimer gameLoop;
    private Timeline notificationTimeline;
    private boolean taskActive = false;
    private boolean inventoryVisible = false;
    private boolean paused = false;
    private long lastUpdate = 0L;

    public GameView(GameState state) {
        this.state = state;
        GameMap map = state.getMap();
        double width = map.getWidth() * GameMap.getTileSize();
        double height = map.getHeight() * GameMap.getTileSize();
        this.canvas = new Canvas(width, height);

        getChildren().add(canvas);
        StackPane.setAlignment(hudOverlay, Pos.TOP_LEFT);
        StackPane.setAlignment(inventoryOverlay, Pos.TOP_RIGHT);
        StackPane.setAlignment(taskDialog, Pos.CENTER);
        StackPane.setAlignment(pauseOverlay, Pos.CENTER);
        StackPane.setAlignment(messageLabel, Pos.BOTTOM_CENTER);

        messageLabel.getStyleClass().add("game-message");
        messageLabel.setWrapText(true);

        getChildren().addAll(hudOverlay, inventoryOverlay, taskDialog, pauseOverlay, messageLabel);

        setFocusTraversable(true);
        setOnKeyPressed(event -> handleKeyPressed(event.getCode()));
        setOnKeyReleased(event -> handleKeyReleased(event.getCode()));

        pauseOverlay.getResumeButton().setOnAction(e -> togglePause(false));
        pauseOverlay.getSaveExitButton().setOnAction(e -> {
            state.save();
            Platform.exit();
        });

        taskDialog.setManaged(false);
        pauseOverlay.setManaged(false);

        taskDialog.hide();
        pauseOverlay.setVisible(false);
        inventoryOverlay.setVisible(false);
        inventoryOverlay.setManaged(false);

        hudOverlay.update(state.getPlayerStats());

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                }
                double delta = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;
                if (!paused && !taskActive) {
                    updatePlayer(delta);
                }
                render();
                hudOverlay.update(state.getPlayerStats());
            }
        };
        gameLoop.start();
    }

    private void handleKeyPressed(KeyCode code) {
        if (code == null) {
            return;
        }
        if (code == KeyCode.ESCAPE) {
            if (taskActive) {
                // ignore ESC while in task - use przycisk Zamknij
                return;
            }
            togglePause(!paused);
            return;
        }
        if (paused) {
            return;
        }
        if (taskActive) {
            return;
        }
        switch (code) {
            case I -> toggleInventory();
            case SPACE -> interact();
            case UP, DOWN, LEFT, RIGHT -> pressedKeys.add(code);
            default -> {
            }
        }
    }

    private void handleKeyReleased(KeyCode code) {
        if (code == null) {
            return;
        }
        if (code == KeyCode.UP || code == KeyCode.DOWN || code == KeyCode.LEFT || code == KeyCode.RIGHT) {
            pressedKeys.remove(code);
        }
    }

    private void togglePause(boolean value) {
        paused = value;
        pauseOverlay.setVisible(value);
        pauseOverlay.setManaged(value);
        if (value) {
            if (notificationTimeline != null) {
                notificationTimeline.stop();
            }
            messageLabel.setText("Gra wstrzymana.");
        } else {
            showNotification(DEFAULT_MESSAGE);
        }
    }

    private void toggleInventory() {
        inventoryVisible = !inventoryVisible;
        inventoryOverlay.update(state.getInventory());
        inventoryOverlay.setVisible(inventoryVisible);
        inventoryOverlay.setManaged(inventoryVisible);
    }

    private void interact() {
        Optional<Obstacle> obstacleOptional = state.findObstacleNearPlayer(GameMap.getTileSize());
        if (obstacleOptional.isEmpty()) {
            showNotification("Brak przeszkód w pobliżu.");
            return;
        }
        Obstacle obstacle = obstacleOptional.get();
        startTask(obstacle);
    }

    private void startTask(Obstacle obstacle) {
        if (inventoryVisible) {
            inventoryVisible = false;
            inventoryOverlay.setVisible(false);
        }
        taskActive = true;
        TaskSession session = state.getTaskManager().createSession(state.getPlayerStats());
        taskDialog.open(obstacle, session, state.getInventory(), new TaskDialog.Listener() {
            @Override
            public void onTaskSuccess(TaskSession session, TaskSession.TaskResult result) {
                handleTaskSuccess(obstacle, session, result);
            }

            @Override
            public void onTaskFailure(TaskSession session, TaskSession.TaskResult result, boolean lifeLost) {
                if (lifeLost) {
                    handleLifeLost();
                }
                inventoryOverlay.update(state.getInventory());
            }

            @Override
            public void onPotionUsed() {
                state.getPlayerStats().gainLife();
                hudOverlay.update(state.getPlayerStats());
                showNotification("Mikstura odnowiła życie. Aktualne życia: " + state.getPlayerStats().getLives());
                inventoryOverlay.update(state.getInventory());
            }

            @Override
            public void onTaskClosed() {
                taskActive = false;
                requestFocus();
            }
        });
        inventoryOverlay.update(state.getInventory());
    }

    private void handleTaskSuccess(Obstacle obstacle, TaskSession session, TaskSession.TaskResult result) {
        state.awardForTask(result.getCorrectCount(), () -> showNotification("Awans! Poziom " + state.getPlayerStats().getLevel()));
        state.markObstacleSolved(obstacle);
        state.removeSolvedObstacles();
        hudOverlay.update(state.getPlayerStats());
        inventoryOverlay.update(state.getInventory());
        showNotification("Przeszkoda pokonana! Zdobyto nagrodę.");
        taskDialog.hide();
        taskActive = false;
        requestFocus();
    }

    private void handleLifeLost() {
        PlayerStats stats = state.getPlayerStats();
        stats.loseLife();
        hudOverlay.update(stats);
        if (!stats.isAlive()) {
            showNotification("Utraciłeś wszystkie życia. Rozpoczynasz od nowa!");
            resetGame();
        } else {
            showNotification("Pozostałe życia: " + stats.getLives());
        }
    }

    private void resetGame() {
        taskDialog.hide();
        taskActive = false;
        state.resetProgress();
        inventoryOverlay.update(state.getInventory());
        inventoryVisible = false;
        inventoryOverlay.setVisible(false);
        inventoryOverlay.setManaged(false);
        hudOverlay.update(state.getPlayerStats());
        pressedKeys.clear();
        lastUpdate = 0L;
        showNotification("Powrót do wioski. Powodzenia!");
    }

    private void updatePlayer(double delta) {
        Player player = state.getPlayer();
        double speed = player.getSpeed();
        double distance = speed * delta;
        double dx = 0;
        double dy = 0;
        if (pressedKeys.contains(KeyCode.LEFT)) {
            dx -= distance;
        }
        if (pressedKeys.contains(KeyCode.RIGHT)) {
            dx += distance;
        }
        if (pressedKeys.contains(KeyCode.UP)) {
            dy -= distance;
        }
        if (pressedKeys.contains(KeyCode.DOWN)) {
            dy += distance;
        }
        movePlayer(player, dx, dy);
    }

    private void movePlayer(Player player, double dx, double dy) {
        if (dx != 0) {
            double newX = player.getX() + dx;
            if (isWalkable(newX, player.getY())) {
                player.setX(newX);
            }
        }
        if (dy != 0) {
            double newY = player.getY() + dy;
            if (isWalkable(player.getX(), newY)) {
                player.setY(newY);
            }
        }
    }

    private boolean isWalkable(double x, double y) {
        GameMap map = state.getMap();
        int tileX = (int) Math.floor(x / GameMap.getTileSize());
        int tileY = (int) Math.floor(y / GameMap.getTileSize());
        if (!map.isWalkable(tileX, tileY)) {
            return false;
        }
        for (Obstacle obstacle : state.getObstacles()) {
            if (!obstacle.isSolved() && obstacle.getTileX() == tileX && obstacle.getTileY() == tileY) {
                return false;
            }
        }
        return true;
    }

    private void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        GameMap map = state.getMap();
        int tileSize = GameMap.getTileSize();

        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                double px = x * tileSize;
                double py = y * tileSize;
                GameMap.Tile tile = map.getTile(x, y);
                switch (tile) {
                    case GROUND -> gc.setFill(Color.web("#d2b48c"));
                    case VILLAGE -> gc.setFill(Color.web("#c0f0c0"));
                    case WATER -> gc.setFill(Color.web("#74b9ff"));
                    default -> gc.setFill(Color.web("#2d3436"));
                }
                gc.fillRect(px, py, tileSize, tileSize);
            }
        }

        for (Obstacle obstacle : state.getObstacles()) {
            if (obstacle.isSolved()) {
                continue;
            }
            double ox = obstacle.getTileX() * tileSize;
            double oy = obstacle.getTileY() * tileSize;
            gc.setFill(Color.web("#b33939"));
            gc.fillRoundRect(ox + 6, oy + 6, tileSize - 12, tileSize - 12, 12, 12);
            gc.setFill(Color.WHITE);
            gc.fillText(obstacle.getType().name().substring(0, 1), ox + tileSize / 2.0 - 4, oy + tileSize / 2.0 + 4);
        }

        Player player = state.getPlayer();
        gc.setFill(Color.web("#0984e3"));
        double radius = tileSize / 3.0;
        gc.fillOval(player.getX() - radius, player.getY() - radius, radius * 2, radius * 2);
    }

    private void showNotification(String message) {
        messageLabel.setText(message);
        if (notificationTimeline != null) {
            notificationTimeline.stop();
        }
        notificationTimeline = new Timeline(new KeyFrame(Duration.seconds(4), event -> messageLabel.setText(DEFAULT_MESSAGE)));
        notificationTimeline.play();
    }

    public void shutdown() {
        gameLoop.stop();
        state.save();
    }
}
