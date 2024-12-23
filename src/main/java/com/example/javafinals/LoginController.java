package com.example.javafinals;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    protected void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if(username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password.");
            return;
        }

        try {
            boolean authenticated = DatabaseController.authenticateUser(username, password);
            if(authenticated) {
                UserSession.getInstance().setUsername(username);
                openChatWindow(username);
                // Optionally, clear the login fields or provide feedback
                // You may also choose to close the login window if desired
                // ((Stage) usernameField.getScene().getWindow()).close();
            } else {
                messageLabel.setText("Invalid username or password.");
            }
        } catch(Exception e) {
            e.printStackTrace();
            messageLabel.setText("An error occurred during login.");
        }
    }

    private void openChatWindow(String username) {
        try {
            // Load the chat FXML
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("chat.fxml"));
            Scene chatScene = new Scene(fxmlLoader.load(), 600, 400); // Adjust size as needed

            // Create a new Stage for the chat window
            Stage chatStage = new Stage();
            chatStage.setTitle("Chat - " + username);
            chatStage.setScene(chatScene);

            // Get the ChatController instance to access WebSocketClient
            ChatController chatController = fxmlLoader.getController();

            // Optionally, pass the username directly if not using UserSession
            // chatController.setUsername(username); // Uncomment if you add a setter

            // Handle window close event to close WebSocket
            chatStage.setOnCloseRequest(event -> {
                if(chatController != null) {
                    chatController.closeWebSocketConnection();
                }
            });

            // Show the chat window
            chatStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Failed to open chat window.");
        }
    }

    @FXML
    protected void goToRegister() {
        try {
            Main.switchScene("registration.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error loading registration page.");
        }
    }
}
