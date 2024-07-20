package org.example.against_strangers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class GuidesController {

    private Parent root;
    private Stage stage;
    private Scene scene;

    @FXML
    private Button backBtn;

    public void backToMenu() throws IOException {

        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("fxml_files/start.fxml")));
        scene = new Scene(root);
        stage = (Stage) backBtn.getScene().getWindow();
        stage.setTitle("Against-Strangers");
        stage.setScene(scene);
        stage.show();

    }

}
