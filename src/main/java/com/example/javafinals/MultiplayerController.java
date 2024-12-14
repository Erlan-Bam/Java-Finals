package com.example.javafinals;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class MultiplayerController {

    @FXML
    private TextField ipField;

    @FXML
    private Label messageLabel;

    @FXML
    protected void handleHost() {
        try {
            // Navigate to the game screen
            Main.switchScene("game.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Failed to host game.");
        }
    }

    @FXML
    protected void handleJoin() {
        String hostIP = ipField.getText().trim();
        if (hostIP.isEmpty()) {
            messageLabel.setText("Please enter the server IP.");
            return;
        }

        // Pass the host IP to GameController
        GameController.setHostIP(hostIP);

        try {
            // Navigate to the game screen
            Main.switchScene("game.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Failed to join game.");
        }
    }
}
