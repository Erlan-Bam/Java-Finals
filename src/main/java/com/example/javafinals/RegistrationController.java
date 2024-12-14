package com.example.javafinals;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegistrationController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    protected void handleRegister() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill all fields.");
            return;
        }

        try {
            if (DatabaseController.registerUser(username, email, password)) {
                messageLabel.setText("Registration successful! You can now log in.");
            } else {
                messageLabel.setText("Username or email already exists.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error connecting to the database.");
        }
    }

    @FXML
    protected void goToLogin() {
        try {
            Main.switchScene("login.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error loading login page.");
        }
    }
}
