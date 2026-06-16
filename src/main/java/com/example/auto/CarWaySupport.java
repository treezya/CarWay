package com.example.auto;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CarWaySupport {

    private CarWaySupport() {
    }

    public static final class AppLogger {

        private AppLogger() {
        }

        public static Logger get(Class<?> type) {
            return Logger.getLogger(type.getName());
        }

        public static void logError(Logger logger, String message, Throwable error) {
            logger.log(Level.SEVERE, message, error);
        }

        public static void logWarning(Logger logger, String message) {
            logger.log(Level.WARNING, message);
        }

        public static void logInfo(Logger logger, String message) {
            logger.log(Level.INFO, message);
        }
    }

    public static final class SceneNavigator {

        private static final Logger LOG = AppLogger.get(SceneNavigator.class);

        private SceneNavigator() {
        }

        public static void navigate(ActionEvent event, Class<?> owner, String resourcePath) {
            navigate(event != null ? (Node) event.getSource() : null, owner, resourcePath);
        }

        public static void navigate(Node sourceNode, Class<?> owner, String resourcePath) {
            try {
                Parent root = loadRoot(owner, resourcePath);
                Stage stage = (Stage) sourceNode.getScene().getWindow();
                Scene scene = stage.getScene();
                if (scene == null) {
                    scene = new Scene(root);
                    stage.setScene(scene);
                } else {
                    scene.setRoot(root);
                }
                stage.show();
            } catch (IOException e) {
                AppLogger.logError(LOG, "Ошибка перехода на экран: " + resourcePath, e);
            }
        }

        public static Stage openNewWindow(Class<?> owner, String resourcePath, String title, int width, int height) {
            try {
                Parent root = loadRoot(owner, resourcePath);
                Stage stage = new Stage();
                stage.setTitle(title);
                stage.setScene(new Scene(root, width, height));
                stage.initModality(Modality.NONE);
                stage.show();
                return stage;
            } catch (IOException e) {
                AppLogger.logError(LOG, "Ошибка открытия окна: " + resourcePath, e);
                return null;
            }
        }

        private static Parent loadRoot(Class<?> owner, String resourcePath) throws IOException {
            URL resource = owner.getResource(resourcePath);
            if (resource == null) {
                throw new IOException("FXML не найден: " + resourcePath);
            }
            return new FXMLLoader(resource).load();
        }
    }
}
