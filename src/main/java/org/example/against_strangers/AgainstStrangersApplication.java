package org.example.against_strangers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class AgainstStrangersApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Image icon = new Image(Objects.requireNonNull(getClass().getResource("images/icon.png")).toExternalForm());
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("fxml_files/start.fxml")));
        Scene scene = new Scene(root);
        stage.setTitle("Against-Strangers");
        stage.setResizable(false);
        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}