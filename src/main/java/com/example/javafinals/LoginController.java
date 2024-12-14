package com.example.javafinals;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    protected void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password.");
            return;
        }

        try {
            if (DatabaseController.authenticateUser(username, password)) {
                messageLabel.setText("Login successful!");
                Main.switchScene("multiplayer.fxml");
            } else {
                messageLabel.setText("Invalid credentials.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error connecting to the database.");
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
