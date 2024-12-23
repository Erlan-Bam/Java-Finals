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
    private char currentPlayer = 'X'; // Start with 'X'
    private boolean gameActive = true;

    @FXML
    public void initialize() {
        initializeGrid();
        statusLabel.setText("Player " + currentPlayer + "'s turn.");
    }

    private void initializeGrid() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Button btn = new Button("");
                btn.setPrefSize(100, 100);
                btn.setStyle("-fx-font-size:24; -fx-font-weight:bold;");

                // Local reference for the row/col
                final int r = row;
                final int c = col;

                btn.setOnAction(e -> handleMove(r, c));

                buttons[row][col] = btn;
                gameGrid.add(btn, col, row);
            }
        }
    }

    private void handleMove(int row, int col) {
        if (!gameActive) {
            return; // If game is over, ignore clicks
        }

        Button btn = buttons[row][col];

        // If this cell is already taken, ignore the click
        if (!btn.getText().isEmpty()) {
            return;
        }

        // Mark the cell with current player's symbol
        btn.setText(String.valueOf(currentPlayer));

        // Check if the current player won
        if (hasPlayerWon(currentPlayer)) {
            statusLabel.setText("Player " + currentPlayer + " wins!");
            gameActive = false;
            return;
        }

        // Check if the board is full => draw
        if (isBoardFull()) {
            statusLabel.setText("It's a draw!");
            gameActive = false;
            return;
        }

        // Switch to the other player
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
        statusLabel.setText("Player " + currentPlayer + "'s turn.");
    }

    private boolean hasPlayerWon(char player) {
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
}
