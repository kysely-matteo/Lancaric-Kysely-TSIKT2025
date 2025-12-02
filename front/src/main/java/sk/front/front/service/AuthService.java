package sk.front.front.service;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import sk.front.front.model.NotificationMessage;
import sk.front.front.model.User;
import sk.front.front.websocket.WebSocketClientService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuthService {
    private static User currentUser;
    private static WebSocketClientService webSocketClient;
    private static ObservableList<NotificationMessage> notifications = FXCollections.observableArrayList();

    public static void setCurrentUser(User user) {
        currentUser = user;

        // Jednoduchá inicializácia WebSocket
        try {
            if (webSocketClient != null) {
                webSocketClient.disconnect();
            }

            webSocketClient = new WebSocketClientService();
            // Použitie lambda namiesto method reference
            webSocketClient.connect(user.getUserId(), notification -> handleNotification(notification));
        } catch (Exception e) {
            System.err.println("Nepodarilo sa inicializovať WebSocket: " + e.getMessage());
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
        // Odpoj WebSocket pri odhlásení
        if (webSocketClient != null) {
            webSocketClient.disconnect();
            webSocketClient = null;
        }
        notifications.clear();
    }

    public static void handleNotification(NotificationMessage notification) {
        Platform.runLater(() -> {
            if (notification.getTimestamp() == null) {
                notification.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            // Pridáme notifikáciu do zoznamu
            notifications.add(0, notification);
            if (notifications.size() > 50) {
                notifications.remove(notifications.size() - 1);
            }
            /* //Len alert thing
            // Zobrazenie notifikácie v alert boxe (môžeme zakázať, ak chceme len v zozname)
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(notification.getTitle());
            alert.setHeaderText("Nové upozornenie");
            alert.setContentText(notification.getMessage());
            alert.showAndWait();
            */
            System.out.println("Prijatá notifikácia: " + notification.getMessage());
        });
    }

    public static void subscribeToGroup(Long groupId) {
        if (webSocketClient != null) {
            webSocketClient.subscribeToGroup(groupId);
        } else {
            System.err.println("WebSocket klient nie je inicializovaný!");
        }
    }

    public static ObservableList<NotificationMessage> getNotifications() {
        return notifications;
    }
}