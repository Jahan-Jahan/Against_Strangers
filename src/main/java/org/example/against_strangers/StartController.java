package org.example.against_strangers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class StartController {

    private Parent root;
    private Stage stage;
    private Scene scene;

    @FXML
    private Button startBtn;

    public void startGame() throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("fxml_files/game.fxml")));
        scene = new Scene(root);
        stage = (Stage) startBtn.getScene().getWindow();
        stage.setTitle("Against-Strangers");
        stage.setScene(scene);
        stage.show();
    }

    public void guides() throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("fxml_files/guides.fxml")));
        scene = new Scene(root);
        stage = (Stage) startBtn.getScene().getWindow();
        stage.setTitle("Guides");
        stage.setScene(scene);
        stage.show();
    }

    public void exitGame() {
        Platform.exit();
    }
}
