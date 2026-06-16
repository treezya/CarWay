package com.example.auto;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public final class CarWayScreensAuth {

    private CarWayScreensAuth() {
    }

    public static class StartController {

        @FXML
        private void goToLogin(ActionEvent event) {
            CarWaySupport.SceneNavigator.navigate(event, getClass(), "/com/example/auto/login-view.fxml");
        }

        @FXML
        private void goToSignup(ActionEvent event) {
            CarWaySupport.SceneNavigator.navigate(event, getClass(), "/com/example/auto/singnup-view.fxml");
        }
    }

    public static class LoginController {

        private static final java.util.logging.Logger LOG = CarWaySupport.AppLogger.get(LoginController.class);

        @FXML
        private Button loginButton;
        @FXML
        private TextField loginField;
        @FXML
        private PasswordField passwordField;

        @FXML
        public void loginUser(ActionEvent event) {
            CarWaySupport.AppLogger.logInfo(LOG, "Нажата кнопка входа");
        }

        @FXML
        private void goToSignup() {
            CarWaySupport.SceneNavigator.navigate(loginButton, getClass(), "/com/example/auto/singnup-view.fxml");
        }

        @FXML
        private void goBackToStart(ActionEvent event) {
            CarWaySupport.SceneNavigator.navigate(event, getClass(), "/com/example/auto/start-view.fxml");
        }

        @FXML
        private void login() {
            if (loginField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()) {
                showInfo("Заполните логин и пароль.");
                return;
            }
            CarWaySupport.SceneNavigator.navigate(loginButton, getClass(), "/com/example/auto/main-view.fxml");
        }

        private void showInfo(String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

    public static class SignupController {

        @FXML
        private TextField phoneField;
        @FXML
        private PasswordField passwordField;
        @FXML
        private PasswordField confirmPasswordField;

        @FXML
        private void goBackToLogin(ActionEvent event) {
            CarWaySupport.SceneNavigator.navigate(event, getClass(), "/com/example/auto/login-view.fxml");
        }

        @FXML
        private void createAccount(ActionEvent event) {
            if (phoneField.getText().trim().isEmpty()
                    || passwordField.getText().trim().isEmpty()
                    || confirmPasswordField.getText().trim().isEmpty()) {
                showInfo("Заполните все поля регистрации.");
                return;
            }
            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                showInfo("Пароли не совпадают.");
                return;
            }
            CarWaySupport.SceneNavigator.navigate(event, getClass(), "/com/example/auto/main-view.fxml");
        }

        private void showInfo(String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
}
