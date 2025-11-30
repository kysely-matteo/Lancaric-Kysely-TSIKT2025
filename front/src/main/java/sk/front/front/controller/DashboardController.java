package sk.front.front.controller;

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

import java.util.List;
import java.util.Optional;

public class DashboardController {
    @FXML private Label welcomeLabel;
    @FXML private ListView<GroupResponse> groupsListView;

    private ApiService apiService = new ApiService();
    private User currentUser;

    @FXML
    private void initialize() {
        currentUser = AuthService.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Vitajte, " + currentUser.getName() + "!");
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
            stage.setScene(new Scene(root, 900, 700));

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
}