package sk.front.front.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import sk.front.front.websocket.WebSocketClientService;
import sk.front.front.model.NotificationMessage;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import sk.front.front.model.GroupResponse;
import sk.front.front.model.User;
import sk.front.front.service.ApiService;
import sk.front.front.service.AuthService;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.*;

import java.util.List;
import java.util.Optional;

public class DashboardController {

    private WebSocketClientService webSocketClient;
    private Long currentUserId;

    public void setCurrentUserId(Long userId) {
        this.currentUserId = userId;
        initializeWebSocket();
    }

    private void initializeWebSocket() {
        // URL k WebSocket serveru, berúc do úvahy tvoj server (localhost:8080)
        String serverUrl = "ws://localhost:8080/ws";
        webSocketClient = new WebSocketClientService();
        webSocketClient.connect(currentUserId, this::handleNotification);
    }

    private void handleNotification(NotificationMessage notification) {
        Platform.runLater(() -> {
            // Zobrazenie notifikácie napr. v Alert boxe
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(notification.getTitle());
            alert.setHeaderText("Nové upozornenie");
            alert.setContentText(notification.getMessage());
            alert.showAndWait();

            // Alebo aktualizovať zoznam notifikácií v UI
            // notificationListView.getItems().add(notification);
        });
    }

    @FXML
    private void handleOpenNotifications() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/notifications.fxml"));
            Parent root = loader.load();

            // The NotificationsController already uses AuthService.getNotifications()
            // so no need to set anything
            Stage stage = new Stage();
            stage.setTitle("Notifikácie");
            stage.setScene(new Scene(root, 400, 500));
            stage.show();

            // Force refresh of ListView when window is opened
            NotificationsController controller = loader.getController();
            if (controller != null) {
                // Refresh the ListView
                controller.notificationListView.refresh();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa otvoriť notifikácie: " + e.getMessage());
        }
    }



    @FXML private Label welcomeLabel;
    @FXML private ListView<GroupResponse> groupsListView;

    private ApiService apiService = new ApiService();
    private User currentUser;

    @FXML
    private void initialize() {
        currentUser = AuthService.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText(currentUser.getName());
            loadUserGroups();
        }
    }

    private void loadUserGroups() {
        try {
            List<GroupResponse> groups = apiService.getUserGroups(currentUser.getUserId());
            groupsListView.getItems().clear();
            groupsListView.getItems().addAll(groups);

            // Format zobrazenia skupín v ListView
            groupsListView.setCellFactory(lv -> new javafx.scene.control.ListCell<GroupResponse>() {
                @Override
                protected void updateItem(GroupResponse group, boolean empty) {
                    super.updateItem(group, empty);
                    if (empty || group == null) {
                        setText(null);
                    } else {
                        setText(group.getName() + " - " + group.getDescription() +
                                " (" + group.getMemberCount() + " členov)");
                    }
                }
            });

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa načítať skupiny: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateGroup() {
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Vytvoriť skupinu");
        nameDialog.setHeaderText("Zadajte názov skupiny");
        nameDialog.setContentText("Názov:");

        Optional<String> nameResult = nameDialog.showAndWait();
        if (nameResult.isEmpty() || nameResult.get().trim().isEmpty()) {
            return;
        }

        TextInputDialog descDialog = new TextInputDialog();
        descDialog.setTitle("Vytvoriť skupinu");
        descDialog.setHeaderText("Zadajte popis skupiny");
        descDialog.setContentText("Popis:");

        Optional<String> descResult = descDialog.showAndWait();
        if (descResult.isEmpty()) {
            return;
        }

        try {
            GroupResponse newGroup = apiService.createGroup(
                    nameResult.get().trim(),
                    descResult.get().trim(),
                    currentUser.getUserId()
            );

            showAlert(Alert.AlertType.INFORMATION, "Úspech", "Skupina '" + newGroup.getName() + "' bola vytvorená!");
            loadUserGroups(); // Refresh zoznamu

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa vytvoriť skupinu: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenGroup() {
        GroupResponse selectedGroup = groupsListView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) {
            showAlert(Alert.AlertType.WARNING, "Upozornenie", "Vyberte skupinu pre otvorenie");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/group-detail.fxml"));
            Parent root = loader.load();

            GroupDetailController controller = loader.getController();
            controller.setGroup(selectedGroup);

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 800));

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa otvoriť skupinu: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshGroups() {
        loadUserGroups();
        showAlert(Alert.AlertType.INFORMATION, "Obnovené", "Zoznam skupín bol obnovený");
    }

    @FXML
    private void handleLogout() {
        try {
            AuthService.logout();
            Parent root = FXMLLoader.load(getClass().getResource("/view/welcome.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa odhlásiť");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void onGroupSelected(Long groupId) {
        AuthService.subscribeToGroup(groupId);
    }

    @FXML
    private void handleEditProfile() {
        try {
            // Vytvorenie custom dialógu
            Dialog<Map<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Upraviť profil");
            dialog.setHeaderText("Zmeňte svoje údaje");

            // Nastavenie tlačidiel
            ButtonType saveButtonType = new ButtonType("Uložiť", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Vytvorenie formulára
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField nameField = new TextField(currentUser.getName());
            nameField.setPromptText("Meno");
            TextField emailField = new TextField(currentUser.getEmail());
            emailField.setPromptText("Email");

            grid.add(new Label("Meno:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Email:"), 0, 1);
            grid.add(emailField, 1, 1);

            dialog.getDialogPane().setContent(grid);

            // Konvertor výsledku
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    Map<String, String> result = new HashMap<>();
                    result.put("name", nameField.getText());
                    result.put("email", emailField.getText());
                    return result;
                }
                return null;
            });

            // Zobrazenie dialógu a spracovanie výsledku
            Optional<Map<String, String>> result = dialog.showAndWait();
            result.ifPresent(userData -> {
                String newName = userData.get("name").trim();
                String newEmail = userData.get("email").trim();

                // Validácia
                if (newName.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Chyba", "Meno nemôže byť prázdne");
                    return;
                }
                if (newEmail.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Chyba", "Email nemôže byť prázdny");
                    return;
                }

                // Kontrola, či sa niečo zmenilo
                if (newName.equals(currentUser.getName()) && newEmail.equals(currentUser.getEmail())) {
                    showAlert(Alert.AlertType.INFORMATION, "Informácia", "Neboli vykonané žiadne zmeny");
                    return;
                }

                try {
                    // Volanie API na update
                    User updatedUser = apiService.updateUser(currentUser.getUserId(), newName, newEmail);

                    // Aktualizácia aktuálneho používateľa
                    AuthService.setCurrentUser(updatedUser);
                    currentUser = updatedUser;

                    // Aktualizácia UI
                    welcomeLabel.setText(currentUser.getName());

                    showAlert(Alert.AlertType.INFORMATION, "Úspech",
                            "Profil bol úspešne aktualizovaný!");

                    // Refresh zoznamu skupín (pre istotu)
                    loadUserGroups();

                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Chyba",
                            "Nepodarilo sa aktualizovať profil: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Chyba",
                    "Nepodarilo sa otvoriť editor profilu: " + e.getMessage());
        }
    }


}