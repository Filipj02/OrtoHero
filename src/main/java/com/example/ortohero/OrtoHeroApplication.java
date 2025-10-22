package com.example.ortohero;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class OrtoHeroApplication extends Application {

    private GameView gameView;

    @Override
    public void start(Stage stage) throws IOException {
        WordRepository repository = new WordRepository();
        GamePersistence persistence = new GamePersistence();
        Optional<SaveData> saveData = persistence.load();
        GameState state = new GameState(repository, persistence, saveData.orElse(null));

        gameView = new GameView(state);
        Scene scene = new Scene(gameView, 960, 640);
        scene.getStylesheets().add(OrtoHeroApplication.class.getResource("/com/example/ortohero/styles.css").toExternalForm());

        stage.setTitle("OrtoHero - edukacyjna gra ortograficzna");
        stage.setScene(scene);
        stage.setResizable(false);
        try (InputStream iconStream = OrtoHeroApplication.class.getResourceAsStream("/com/example/ortohero/icon.png")) {
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
        }
        stage.show();

        gameView.requestFocus();
        stage.setOnCloseRequest(event -> gameView.shutdown());
    }

    public static void main(String[] args) {
        launch();
    }
}
