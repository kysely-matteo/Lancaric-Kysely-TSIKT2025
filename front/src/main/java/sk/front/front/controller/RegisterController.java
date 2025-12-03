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

public class RegisterController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    private ApiService apiService = new ApiService();

    @FXML
    private void handleRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Vyplňte všetky povinné polia");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Heslá sa nezhodujú");
            return;
        }

        if (password.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Heslo musí mať aspoň 6 znakov");
            return;
        }

        try {
            User user = apiService.register(name, email, password);
            AuthService.setCurrentUser(user);
            showAlert(Alert.AlertType.INFORMATION, "Úspech", "Úspešne zaregistrovaný!");
            loadDashboard();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba registrácie", e.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa vrátiť na prihlásenie");
        }
    }

    private void loadDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/dashboard.fxml"));
            Stage stage = (Stage) nameField.getScene().getWindow();
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
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa vrátiť");
        }
    }
}