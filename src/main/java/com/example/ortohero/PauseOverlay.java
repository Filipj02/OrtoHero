package com.example.ortohero;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PauseOverlay extends VBox {
    private final Button resumeButton = new Button("Wznów");
    private final Button saveExitButton = new Button("Zapisz i wyjdź");

    public PauseOverlay() {
        setAlignment(Pos.CENTER);
        setSpacing(12);
        getStyleClass().add("pause-overlay");

        Label title = new Label("Pauza");
        title.getStyleClass().add("pause-title");

        getChildren().addAll(title, resumeButton, saveExitButton);
        setVisible(false);
        setPickOnBounds(false);
    }

    public Button getResumeButton() {
        return resumeButton;
    }

    public Button getSaveExitButton() {
        return saveExitButton;
    }
}
