package com.example.ortohero;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HUDOverlay extends VBox {
    private final Label livesLabel = new Label();
    private final ProgressBar xpBar = new ProgressBar();
    private final Label xpLabel = new Label();
    private final Label levelLabel = new Label();
    private final Label goldLabel = new Label();

    public HUDOverlay() {
        setSpacing(6);
        setPadding(new Insets(12));
        setAlignment(Pos.TOP_LEFT);
        getStyleClass().add("hud-overlay");

        livesLabel.getStyleClass().add("hud-lives");
        xpBar.setPrefWidth(200);
        xpBar.getStyleClass().add("hud-xp");
        xpLabel.getStyleClass().add("hud-text");
        levelLabel.getStyleClass().add("hud-text");
        goldLabel.getStyleClass().add("hud-text");

        getChildren().addAll(livesLabel, xpBar, xpLabel, levelLabel, goldLabel);
    }

    public void update(PlayerStats stats) {
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < stats.getLives(); i++) {
            hearts.append("\u2764\uFE0F ");
        }
        for (int i = stats.getLives(); i < stats.getMaxLives(); i++) {
            hearts.append("\u2661 ");
        }
        livesLabel.setText("Życia: " + hearts.toString().trim());
        xpBar.setProgress(stats.getXpProgress());
        xpLabel.setText("XP: " + stats.getXp() + "/100");
        levelLabel.setText("Poziom: " + stats.getLevel());
        goldLabel.setText("Złoto: " + stats.getGold());
    }
}
