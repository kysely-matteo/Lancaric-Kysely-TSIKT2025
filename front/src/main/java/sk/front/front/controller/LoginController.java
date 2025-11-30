package sk.front.front.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sk.front.front.model.User;
import sk.front.front.service.ApiService;
import sk.front.front.service.AuthService;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private ApiService apiService = new ApiService();

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Vyplňte všetky polia");
            return;
        }

        try {
            User user = apiService.login(email, password);
            AuthService.setCurrentUser(user);
            showAlert(Alert.AlertType.INFORMATION, "Úspech", "Úspešne prihlásený!");
            loadDashboard();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba prihlásenia", e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/register.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa načítať registráciu");
        }
    }

    private void loadDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/dashboard.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa načítať dashboard");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleBackToWelcome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/welcome.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa vrátiť");
        }
    }
}
