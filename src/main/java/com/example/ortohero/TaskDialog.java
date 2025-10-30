package com.example.ortohero;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TaskDialog extends BorderPane {

    public interface Listener {
        void onTaskSuccess(TaskSession session, TaskSession.TaskResult result);

        void onTaskFailure(TaskSession session, TaskSession.TaskResult result, boolean lifeLost);

        void onPotionUsed();

        void onTaskClosed();
    }

    private final Label titleLabel = new Label();
    private final Label requirementLabel = new Label();
    private final VBox wordsBox = new VBox(8);
    private final Label infoLabel = new Label();
    private final Button checkButton = new Button("Sprawdź");
    private final Button hintButton = new Button("Podpowiedź");
    private final Button closeButton = new Button("Zamknij");

    private final Button swordButton = new Button();
    private final Button wandButton = new Button();
    private final Button armorButton = new Button();
    private final Button potionButton = new Button();
    private final Button featherButton = new Button();

    private final List<WordRow> rows = new ArrayList<>();

    private TaskSession session;
    private Inventory inventory;
    private Listener listener;

    public TaskDialog() {
        getStyleClass().add("task-dialog");
        setVisible(false);
        setPadding(new Insets(20));

        VBox header = new VBox(4, titleLabel, requirementLabel);
        header.getStyleClass().add("task-header");

        BorderPane.setAlignment(header, Pos.TOP_LEFT);
        setTop(header);

        wordsBox.getStyleClass().add("task-words");
        setCenter(wordsBox);

        VBox bottom = new VBox(8);
        bottom.setAlignment(Pos.CENTER_LEFT);

        HBox actionButtons = new HBox(8, checkButton, hintButton, closeButton);
        actionButtons.setAlignment(Pos.CENTER_LEFT);

        HBox itemButtons = new HBox(8, swordButton, wandButton, armorButton, potionButton, featherButton);
        itemButtons.setAlignment(Pos.CENTER_LEFT);
        itemButtons.getStyleClass().add("task-items");

        infoLabel.getStyleClass().add("task-info");

        bottom.getChildren().addAll(itemButtons, actionButtons, infoLabel);
        setBottom(bottom);

        configureButtons();
    }

    private void configureButtons() {
        checkButton.setOnAction(event -> handleCheck());
        hintButton.setOnAction(event -> handleHint());
        closeButton.setOnAction(event -> {
            if (listener != null) {
                listener.onTaskClosed();
            }
            hide();
        });

        swordButton.setOnAction(event -> handleSword());
        wandButton.setOnAction(event -> handleWand());
        armorButton.setOnAction(event -> handleArmor());
        potionButton.setOnAction(event -> handlePotion());
        featherButton.setOnAction(event -> handleFeather());
    }

    public void open(Obstacle obstacle, TaskSession session, Inventory inventory, Listener listener) {
        this.session = session;
        this.inventory = inventory;
        this.listener = listener;
        titleLabel.setText(obstacle.getType().getDisplayName());
        requirementLabel.setText("Wpisz poprawnie " + session.getRequiredCorrect() + " słów, aby kontynuować.");
        infoLabel.setText("Zadanie: " + obstacle.getType().getDescription());
        infoLabel.getStyleClass().removeAll("task-info-success", "task-info-error");
        rows.clear();
        wordsBox.getChildren().clear();

        for (TaskWord word : session.getWords()) {
            WordRow row = new WordRow(word);
            rows.add(row);
            wordsBox.getChildren().add(row.getContainer());
        }

        updateInventoryButtons();
        updateHintButton();
        setManaged(true);
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
        setManaged(false);
    }

    private void handleCheck() {
        if (session == null) {
            return;
        }
        TaskSession.TaskResult result = session.evaluate();
        applyResultStyles(result);
        if (result.isSuccess(session.getRequiredCorrect())) {
            showInfo("Poprawnie! +50 XP", true);
            if (listener != null) {
                listener.onTaskSuccess(session, result);
            }
        } else {
            if (session.consumeArmorCharge()) {
                showInfo("Zbroja ochroniła przed utratą życia! Popraw odpowiedzi.", false);
                if (listener != null) {
                    listener.onTaskFailure(session, result, false);
                }
            } else {
                showInfo("Błąd ortograficzny. Skorzystaj z podpowiedzi lub popraw wpisy.", false);
                if (listener != null) {
                    listener.onTaskFailure(session, result, true);
                }
            }
        }
    }

    private void applyResultStyles(TaskSession.TaskResult result) {
        for (int i = 0; i < rows.size(); i++) {
            boolean incorrect = result.getIncorrectIndices().contains(i);
            rows.get(i).setIncorrect(incorrect);
        }
    }

    private void handleHint() {
        if (session == null || !session.useHintAvailable()) {
            showInfo("Podpowiedź została już użyta.", false);
            return;
        }
        Optional<String> hint = session.requestHint();
        hint.ifPresentOrElse(value -> {
            showInfo("Podpowiedź: " + value, true);
        }, () -> {
            showInfo("Brak słów wymagających podpowiedzi.", false);
        });
        updateHintButton();
    }

    private void handleSword() {
        if (session == null || inventory == null) {
            return;
        }
        if (session.isSwordUsed()) {
            showInfo("Miecz został już użyty.", false);
            return;
        }
        if (inventory.getQuantity(ItemType.SWORD) <= 0) {
            showInfo("Brak Miecza w ekwipunku.", false);
            return;
        }
        if (session.useSword()) {
            inventory.consume(ItemType.SWORD);
            requirementLabel.setText("Wpisz poprawnie " + session.getRequiredCorrect() + " słowa.");
            showInfo("Miecz skraca zadanie do " + session.getRequiredCorrect() + " słów!", true);
        } else {
            showInfo("Nie możesz użyć Miecza teraz.", false);
        }
        updateInventoryButtons();
    }

    private void handleWand() {
        if (session == null || inventory == null) {
            return;
        }
        List<WordRow> eligible = rows.stream()
                .filter(row -> row.getTaskWord().getEntry().getDifficulty() > 1)
                .collect(Collectors.toList());
        if (eligible.isEmpty()) {
            showInfo("Brak trudnych słów do podmiany.", false);
            return;
        }
        ChoiceDialog<WordRow> dialog = new ChoiceDialog<>(eligible.get(0), eligible);
        dialog.setTitle("Różdżka");
        dialog.setHeaderText("Wybierz słowo do podmiany");
        dialog.setContentText("Słowo:");
        dialog.setConverter(new StringConverter<>() {
            @Override
            public String toString(WordRow object) {
                return object == null ? "" : object.getTaskWord().getEntry().getPattern();
            }

            @Override
            public WordRow fromString(String string) {
                return null;
            }
        });
        Optional<WordRow> selection = dialog.showAndWait();
        if (selection.isPresent()) {
            WordRow selected = selection.get();
            if (inventory.getQuantity(ItemType.WAND) <= 0) {
                showInfo("Brak Różdżki w ekwipunku.", false);
                return;
            }
            Optional<TaskWord> replacement = session.useWand(selected.getTaskWord());
            if (replacement.isPresent()) {
                inventory.consume(ItemType.WAND);
                selected.updateTaskWord(replacement.get());
                showInfo("Różdżka zamieniła słowo na łatwiejsze.", true);
            } else {
                showInfo("Nie udało się znaleźć łatwiejszego słowa.", false);
            }
        }
        updateInventoryButtons();
    }

    private void handleArmor() {
        if (session == null || inventory == null) {
            return;
        }
        if (session.isArmorUsed()) {
            showInfo("Zbroja już aktywna w tym zadaniu.", false);
            return;
        }
        if (inventory.getQuantity(ItemType.ARMOR) <= 0) {
            showInfo("Brak Zbroi w ekwipunku.", false);
            return;
        }
        if (session.activateArmor()) {
            inventory.consume(ItemType.ARMOR);
            showInfo("Zbroja aktywna - pierwsza porażka za darmo.", true);
            armorButton.setDisable(true);
        } else {
            showInfo("Zbroja nie może zostać użyta ponownie.", false);
        }
        updateInventoryButtons();
    }

    private void handlePotion() {
        if (session == null || inventory == null) {
            return;
        }
        if (inventory.getQuantity(ItemType.POTION) <= 0) {
            showInfo("Brak mikstur.", false);
        } else {
            inventory.consume(ItemType.POTION);
            showInfo("Mikstura przywraca 1 życie.", true);
            if (listener != null) {
                listener.onPotionUsed();
            }
        }
        updateInventoryButtons();
    }

    private void handleFeather() {
        if (session == null || inventory == null) {
            return;
        }
        if (rows.isEmpty()) {
            return;
        }
        ChoiceDialog<WordRow> dialog = new ChoiceDialog<>(rows.get(0), rows);
        dialog.setTitle("Pióro Mądrości");
        dialog.setHeaderText("Wybierz słowo do odsłonięcia litery");
        dialog.setContentText("Słowo:");
        dialog.setConverter(new StringConverter<>() {
            @Override
            public String toString(WordRow object) {
                return object == null ? "" : object.getTaskWord().getEntry().getPattern();
            }

            @Override
            public WordRow fromString(String string) {
                return null;
            }
        });
        Optional<WordRow> selection = dialog.showAndWait();
        if (selection.isPresent()) {
            if (inventory.getQuantity(ItemType.FEATHER) <= 0) {
                showInfo("Nie masz Pióra Mądrości.", false);
            } else {
                WordRow row = selection.get();
                Optional<Character> letter = session.useFeather(row.getTaskWord());
                if (letter.isPresent()) {
                    inventory.consume(ItemType.FEATHER);
                    row.refreshAnswer();
                    showInfo("Pióro ujawniło literę: " + letter.get(), true);
                } else {
                    showInfo("Brak liter do ujawnienia w tym słowie.", false);
                }
            }
        }
        updateInventoryButtons();
    }

    private void updateInventoryButtons() {
        if (inventory == null) {
            swordButton.setDisable(true);
            wandButton.setDisable(true);
            armorButton.setDisable(true);
            potionButton.setDisable(true);
            featherButton.setDisable(true);
            return;
        }
        swordButton.setText("Miecz (" + inventory.getQuantity(ItemType.SWORD) + ")");
        wandButton.setText("Różdżka (" + inventory.getQuantity(ItemType.WAND) + ")");
        if (session != null && session.isArmorUsed()) {
            armorButton.setText("Zbroja (aktywna)");
        } else {
            armorButton.setText("Zbroja (" + inventory.getQuantity(ItemType.ARMOR) + ")");
        }
        potionButton.setText("Mikstura (" + inventory.getQuantity(ItemType.POTION) + ")");
        featherButton.setText("Pióro (" + inventory.getQuantity(ItemType.FEATHER) + ")");

        swordButton.setDisable(session == null || session.isSwordUsed() || inventory.getQuantity(ItemType.SWORD) == 0);
        wandButton.setDisable(session == null || inventory.getQuantity(ItemType.WAND) == 0);
        armorButton.setDisable(session == null || session.isArmorUsed() || inventory.getQuantity(ItemType.ARMOR) == 0);
        potionButton.setDisable(session == null || inventory.getQuantity(ItemType.POTION) == 0);
        featherButton.setDisable(session == null || inventory.getQuantity(ItemType.FEATHER) == 0);
        updateHintButton();
    }

    private void showInfo(String text, Boolean successStyle) {
        infoLabel.setText(text);
        infoLabel.getStyleClass().removeAll("task-info-success", "task-info-error");
        if (Boolean.TRUE.equals(successStyle)) {
            infoLabel.getStyleClass().add("task-info-success");
        } else if (Boolean.FALSE.equals(successStyle)) {
            infoLabel.getStyleClass().add("task-info-error");
        }
    }

    private void updateHintButton() {
        if (session == null) {
            hintButton.setDisable(true);
        } else {
            hintButton.setDisable(!session.useHintAvailable());
        }
    }

    private static class WordRow {
        private TaskWord taskWord;
        private final HBox container;
        private final Label patternLabel = new Label();
        private final TextField answerField = new TextField();

        WordRow(TaskWord word) {
            this.taskWord = word;
            this.container = new HBox(10);
            container.setAlignment(Pos.CENTER_LEFT);
            container.getStyleClass().add("task-word-row");
            patternLabel.getStyleClass().add("task-pattern");
            answerField.getStyleClass().add("task-answer");
            container.getChildren().addAll(patternLabel, answerField);
            bindWord(word);
        }

        private void bindWord(TaskWord word) {
            patternLabel.setText(word.getEntry().getPattern());
            if (taskWord != null) {
                answerField.textProperty().unbindBidirectional(taskWord.answerProperty());
            }
            taskWord = word;
            answerField.textProperty().bindBidirectional(taskWord.answerProperty());
        }

        public void updateTaskWord(TaskWord newWord) {
            bindWord(newWord);
            answerField.clear();
        }

        public void refreshAnswer() {
            answerField.setText(taskWord.getAnswer());
        }

        @Override
        public String toString() {
            return taskWord == null ? "" : taskWord.getEntry().getPattern();
        }

        public void setIncorrect(boolean incorrect) {
            if (incorrect) {
                container.getStyleClass().add("task-word-incorrect");
            } else {
                container.getStyleClass().remove("task-word-incorrect");
            }
        }

        public HBox getContainer() {
            return container;
        }

        public TaskWord getTaskWord() {
            return taskWord;
        }
    }
}
