package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controller.LibraryController;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/library_view.fxml"));
        Parent root = loader.load();
        LibraryController controller = loader.getController();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Könyvtárkezelő");
        stage.setMinHeight(600);
        stage.setMinWidth(800);
        stage.show();

    }

    public static void main(String[] args) {
        launch();

    }
}