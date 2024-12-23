package com.example.javafinals;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class MultiplayerController {

    @FXML
    private TextField ipField;

    @FXML
    private Label messageLabel;

    /**
     * Host the game on your public IP (assuming port forwarding is set up).
     * The GameController will act as a server on port 55555.
     */
    @FXML
    protected void handleHost() {
        // We set hostIP to null or empty, signaling the GameController
        // that this machine is the host.
        GameController.setHostIP(null);

        try {
            Main.switchScene("game.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Failed to host game.");
        }
    }

    /**
     * Join a remote game. If the user doesn't type anything,
     * default to your public IP for demonstration.
     */
    @FXML
    protected void handleJoin() {
        String hostIP = ipField.getText().trim();

        // If empty, we’ll default to your public IP (37.99.36.39)
        // so you can test quickly.
        if (hostIP.isEmpty()) {
            hostIP = "37.99.36.39";
            ipField.setText(hostIP);
        }

        // Pass the host IP to GameController
        GameController.setHostIP(hostIP);

        try {
            Main.switchScene("game.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Failed to join game.");
        }
    }
}
