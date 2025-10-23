package com.example.ortohero;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class InventoryOverlay extends VBox {
    private final VBox content = new VBox(8);

    public InventoryOverlay() {
        setAlignment(Pos.TOP_RIGHT);
        setPadding(new Insets(16));
        setPickOnBounds(false);
        getStyleClass().add("inventory-overlay");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(220, 260);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("inventory-scroll");

        Label title = new Label("Ekwipunek");
        title.getStyleClass().add("inventory-title");

        getChildren().addAll(title, scrollPane);
        setVisible(false);
    }

    public void update(Inventory inventory) {
        content.getChildren().clear();
        inventory.getItems().forEach((type, qty) -> {
            if (qty > 0) {
                Label label = new Label(type.getDisplayName() + " x" + qty + "\n" + type.getDescription());
                label.getStyleClass().add("inventory-item");
                content.getChildren().add(label);
            }
        });
        if (content.getChildren().isEmpty()) {
            Label empty = new Label("Brak przedmiotów");
            empty.getStyleClass().add("inventory-empty");
            content.getChildren().add(empty);
        }
    }
}
