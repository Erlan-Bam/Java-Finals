package com.example.javafinals;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class GameController {

    @FXML
    private GridPane gameGrid;

    @FXML
    private Label statusLabel;

    private Button[][] buttons = new Button[3][3];
    private char currentMark = 'X'; // Start with 'X'

    @FXML
    public void initialize() {
        initializeGrid();
        statusLabel.setText("Player X's turn.");
    }

    private void initializeGrid() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Button btn = new Button("");
                btn.setPrefSize(100, 100);
                btn.setStyle("-fx-font-size:24; -fx-font-weight:bold;");
                final int r = row;
                final int c = col;
                btn.setOnAction(e -> handleMove(r, c));
                buttons[row][col] = btn;
                gameGrid.add(btn, col, row);
            }
        }
    }

    private void handleMove(int row, int col) {
        Button btn = buttons[row][col];
        if (!btn.getText().isEmpty()) {
            return; // Prevent overwriting a move
        }

        btn.setText(String.valueOf(currentMark)); // Set the current player's mark
        if (checkWin(currentMark)) {
            statusLabel.setText("Player " + currentMark + " wins!");
            disableAllButtons();
        } else if (isBoardFull()) {
            statusLabel.setText("It's a draw!");
            disableAllButtons();
        } else {
            // Switch turns
            currentMark = (currentMark == 'X') ? 'O' : 'X';
            statusLabel.setText("Player " + currentMark + "'s turn.");
        }
    }

    private boolean checkWin(char player) {
        // Check rows
        for (int r = 0; r < 3; r++) {
            if (buttons[r][0].getText().equals(String.valueOf(player)) &&
                    buttons[r][1].getText().equals(String.valueOf(player)) &&
                    buttons[r][2].getText().equals(String.valueOf(player))) {
                return true;
            }
        }
        // Check columns
        for (int c = 0; c < 3; c++) {
            if (buttons[0][c].getText().equals(String.valueOf(player)) &&
                    buttons[1][c].getText().equals(String.valueOf(player)) &&
                    buttons[2][c].getText().equals(String.valueOf(player))) {
                return true;
            }
        }
        // Check diagonals
        if (buttons[0][0].getText().equals(String.valueOf(player)) &&
                buttons[1][1].getText().equals(String.valueOf(player)) &&
                buttons[2][2].getText().equals(String.valueOf(player))) {
            return true;
        }
        if (buttons[0][2].getText().equals(String.valueOf(player)) &&
                buttons[1][1].getText().equals(String.valueOf(player)) &&
                buttons[2][0].getText().equals(String.valueOf(player))) {
            return true;
        }
        return false;
    }

    private boolean isBoardFull() {
        for (Button[] row : buttons) {
            for (Button btn : row) {
                if (btn.getText().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void disableAllButtons() {
        for (Button[] row : buttons) {
            for (Button btn : row) {
                btn.setDisable(true);
            }
        }
    }
}
