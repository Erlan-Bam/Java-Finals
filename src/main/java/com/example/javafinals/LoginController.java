package com.example.javafinals;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

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
                Main.switchScene("chat.fxml");
            } else {
                messageLabel.setText("Invalid username or password.");
            }
        } catch(Exception e) {
            e.printStackTrace();
            messageLabel.setText("An error occurred during login.");
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
