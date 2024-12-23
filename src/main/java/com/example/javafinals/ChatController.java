package com.example.javafinals;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;

public class ChatController {

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField messageField;

    @FXML
    private TextField receiverField;

    private WebSocketClient webSocketClient;

    private String currentUser; // Current user's username

    @FXML
    public void initialize() {
        // Retrieve the current username from UserSession
        currentUser = UserSession.getInstance().getUsername();
        if(currentUser == null || currentUser.isEmpty()) {
            chatArea.appendText("User not logged in.\n");
            return;
        }
        connectWebSocket();
    }

    private void connectWebSocket() {
        try {
            // Replace with your server's URI
            URI serverUri = new URI("ws://localhost:8887"); // Change as needed
            webSocketClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to chat server");
                    // Register the current user
                    JSONObject registerJson = new JSONObject();
                    registerJson.put("type", "register");
                    registerJson.put("username", currentUser);
                    send(registerJson.toString());
                }

                @Override
                public void onMessage(String message) {
                    Platform.runLater(() -> handleIncomingMessage(message));
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected from chat server: " + reason);
                    Platform.runLater(() -> chatArea.appendText("Disconnected from chat server.\n"));
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> chatArea.appendText("Error: " + ex.getMessage() + "\n"));
                }
            };
            webSocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
            chatArea.appendText("Failed to connect to chat server.\n");
        }
    }

    @FXML
    protected void handleSend() {
        String message = messageField.getText().trim();
        String receiver = receiverField.getText().trim();

        if (message.isEmpty() || receiver.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter both receiver and message.");
            return;
        }

        JSONObject chatJson = new JSONObject();
        chatJson.put("type", "chat");
        chatJson.put("sender", currentUser);
        chatJson.put("receiver", receiver);
        chatJson.put("message", message);

        webSocketClient.send(chatJson.toString());

        // Display the sent message in the chat area
        chatArea.appendText("You to " + receiver + ": " + message + "\n");

        // Clear the message field
        messageField.clear();
    }

    private void handleIncomingMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.getString("type");

            switch (type) {
                case "chat":
                    String sender = json.getString("sender");
                    String chatMessage = json.getString("message");
                    chatArea.appendText(sender + ": " + chatMessage + "\n");
                    break;

                case "history":
                    // Implement chat history handling if needed
                    break;

                case "error":
                    String errorMsg = json.getString("message");
                    chatArea.appendText("Error: " + errorMsg + "\n");
                    break;

                default:
                    System.out.println("Unknown message type: " + type);
            }
        } catch (Exception e) {
            e.printStackTrace();
            chatArea.appendText("Received malformed message.\n");
        }
    }

    public void closeWebSocketConnection() {
        if(webSocketClient != null && !webSocketClient.isClosed()) {
            webSocketClient.close();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    protected void goToGame() {
        try {
            // Load the game FXML
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("game.fxml"));
            Scene gameScene = new Scene(fxmlLoader.load(), 600, 400); // Adjust size as needed

            // Create a new Stage for the game window
            Stage gameStage = new Stage();
            gameStage.setTitle("Game - " + currentUser);
            gameStage.setScene(gameScene);
            gameStage.initModality(Modality.NONE); // You can change modality as needed

            // Optionally, pass data to the GameController if needed
            // GameController gameController = fxmlLoader.getController();
            // gameController.initializeGame(currentUser);

            // Show the game window
            gameStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open game window.");
        }
    }
}
