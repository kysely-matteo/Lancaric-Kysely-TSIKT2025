package sk.front.front.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import sk.front.front.model.NotificationMessage;
import sk.front.front.service.AuthService;

import java.net.URL;
import java.util.ResourceBundle;

public class NotificationsController implements Initializable {

    @FXML
    ListView<NotificationMessage> notificationListView;

    @FXML
    private Label statusLabel;

    @FXML
    private Button clearButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Nastavíme ListView
        notificationListView.setItems(AuthService.getNotifications());

        // Nastavíme custom cell factory na správne zobrazenie notifikácií
        notificationListView.setCellFactory(param -> new ListCell<NotificationMessage>() {
            @Override
            protected void updateItem(NotificationMessage item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Format the notification display
                    String displayText = String.format(
                            "[%s] %s\n%s\n%s",
                            item.getType(),
                            item.getTitle(),
                            item.getMessage(),
                            item.getTimestamp() != null ? item.getTimestamp() : ""
                    );
                    setText(displayText);
                    setWrapText(true);
                }
            }
        });

        // Aktualizujeme status
        updateStatus();

        // Pridáme listener na zmenu veľkosti zoznamu
        AuthService.getNotifications().addListener((javafx.collections.ListChangeListener.Change<? extends NotificationMessage> change) -> {
            updateStatus();
        });
    }

    private void updateStatus() {
        int count = AuthService.getNotifications().size();
        statusLabel.setText(count + " notifikácií");

        if (count == 0) {
            clearButton.setDisable(true);
        } else {
            clearButton.setDisable(false);
        }
    }

    @FXML
    private void handleClearAll() {
        AuthService.getNotifications().clear();
    }
}