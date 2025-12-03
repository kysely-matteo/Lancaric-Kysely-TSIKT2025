package sk.front.front.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import sk.front.front.model.NotificationMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class WebSocketClientService {
    private WebSocket webSocket;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Consumer<NotificationMessage> onNotificationCallback;
    private String serverUrl = "ws://localhost:8080/ws";
    private Long userId;

    public void connect(Long userId, Consumer<NotificationMessage> onNotification) {
        this.userId = userId;
        System.out.println("DEBUG [CLIENT]: Pokúšam sa pripojiť na: " + serverUrl + "?userId=" + userId);

        this.onNotificationCallback = onNotification;
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(serverUrl + "?userId=" + userId);

        client.newWebSocketBuilder()
                .buildAsync(uri, new WebSocket.Listener() {
                    private StringBuilder buffer = new StringBuilder();

                    @Override
                    public void onOpen(WebSocket webSocket) {
                        System.out.println("DEBUG [CLIENT]: WebSocket ÚSPEŠNE pripojený!");
                        WebSocketClientService.this.webSocket = webSocket;

                        String connectFrame = "CONNECT\naccept-version:1.2\nhost:localhost\n\n\0";
                        webSocket.sendText(connectFrame, true);
                        System.out.println("DEBUG [CLIENT]: Odoslaná CONNECT správa");

                        WebSocket.Listener.super.onOpen(webSocket);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        buffer.append(data);
                        if (last) {
                            String fullMessage = buffer.toString();
                            buffer = new StringBuilder();
                            System.out.println("DEBUG [CLIENT]: Prijatá správa: " + fullMessage);
                            processIncomingMessage(fullMessage);
                        }
                        return WebSocket.Listener.super.onText(webSocket, data, last);
                    }

                    @Override
                    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                        System.out.println("DEBUG [CLIENT]: WebSocket odpojený. Dôvod: " + reason);
                        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        System.err.println("DEBUG [CLIENT]: WebSocket chyba: " + error.getMessage());
                        WebSocket.Listener.super.onError(webSocket, error);
                    }
                });
    }

    private void processIncomingMessage(String message) {
        System.out.println("DEBUG [CLIENT]: ==== STOMP SPRÁVA ====");
        System.out.println(message);
        System.out.println("DEBUG [CLIENT]: ==== KONIEC SPRÁVY ====");

        // Spracuj STOMP správy
        if (message.startsWith("CONNECTED")) {
            System.out.println("DEBUG [CLIENT]: STOMP pripojený úspešne!");

            // Predplatíme si kanál pre používateľa
            String subscribeUser = "SUBSCRIBE\nid:sub-user\ndestination:/topic/user/" + userId + "\n\n\0";
            webSocket.sendText(subscribeUser, true);
            System.out.println("DEBUG [CLIENT]: Odoslaná SUBSCRIBE pre používateľa");

            // DÔLEŽITÉ: Predplatíme si VŠETKY skupiny (wildcard *)
            String subscribeAllGroups = "SUBSCRIBE\nid:sub-groups\ndestination:/topic/group/*\n\n\0";
            webSocket.sendText(subscribeAllGroups, true);
            System.out.println("DEBUG [CLIENT]: Odoslaná SUBSCRIBE pre VŠETKY skupiny");

            return;
        }

        if (message.startsWith("MESSAGE\n")) {
            try {
                System.out.println("DEBUG [CLIENT]: Našiel som MESSAGE správu!");

                // STOMP správa má hlavičku a telo oddelené dvoma \n\n
                String[] parts = message.split("\n\n", 2);
                if (parts.length > 1) {
                    String jsonBody = parts[1];
                    // Odstrániť null character na konci
                    jsonBody = jsonBody.replace("\u0000", "");

                    System.out.println("DEBUG [CLIENT]: JSON telo: " + jsonBody);

                    NotificationMessage notification = objectMapper.readValue(jsonBody, NotificationMessage.class);

                    Platform.runLater(() -> {
                        if (onNotificationCallback != null) {
                            onNotificationCallback.accept(notification);
                        }
                    });
                }
            } catch (Exception e) {
                System.err.println("DEBUG [CLIENT]: Chyba pri parsovaní: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Spracovanie chyby
        if (message.startsWith("ERROR\n")) {
            System.err.println("DEBUG [CLIENT]: STOMP chyba: " + message);
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Odpojenie z klienta")
                    .thenRun(() -> System.out.println("WebSocket úspešne odpojený"));
        }
    }

    public void subscribeToGroup(Long groupId) {
        System.out.println("DEBUG [CLIENT]: Odoberám skupinu: " + groupId);

        if (webSocket != null) {
            // Odoslať STOMP SUBSCRIBE správu pre konkrétnu skupinu
            String subscribeFrame = "SUBSCRIBE\nid:sub-group-" + groupId +
                    "\ndestination:/topic/group/" + groupId + "\n\n\0";
            webSocket.sendText(subscribeFrame, true);
            System.out.println("DEBUG [CLIENT]: Odoslaná SUBSCRIBE pre skupinu " + groupId);
        } else {
            System.err.println("DEBUG [CLIENT]: WebSocket nie je pripojený!");
        }
    }
}