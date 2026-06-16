package com.example.auto;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;



public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("/com/example/auto/start-view.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("CarWay");
        stage.setMinWidth(900);
        stage.setMinHeight(520);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        var logging = HelloApplication.class.getResource("/logging.properties");
        if (logging != null) {
            System.setProperty("java.util.logging.config.file", logging.toExternalForm());
        }
        launch();
    }
}
