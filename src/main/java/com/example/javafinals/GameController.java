package com.example.javafinals;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class GameController {

    @FXML
    private GridPane gameGrid;

    @FXML
    private Label statusLabel;

    private Button[][] buttons = new Button[3][3];
    private char myMark;
    private char opponentMark;
    private boolean myTurn;

    // Networking
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    // Host IP (if joining). If null -> we are hosting.
    private static String hostIP = null;

    public static void setHostIP(String ip) {
        hostIP = ip;
    }

    @FXML
    public void initialize() {
        initializeGrid();

        if (hostIP == null) {
            // We are HOSTING
            myMark = 'X';
            opponentMark = 'O';
            myTurn = true;
            statusLabel.setText("Hosting game on port 55555... Waiting for opponent to connect.");

            // Start the server in a separate thread
            new Thread(this::hostGame).start();
        } else {
            // We are JOINING
            myMark = 'O';
            opponentMark = 'X';
            myTurn = false;
            statusLabel.setText("Joining game at " + hostIP + ":55555...");

            // Start the client connection in a separate thread
            new Thread(() -> joinGame(hostIP)).start();
        }
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
        if (!myTurn) return;
        Button btn = buttons[row][col];
        if (!btn.getText().isEmpty()) return;

        btn.setText(String.valueOf(myMark));
        sendMove(row, col);
        myTurn = false;
        statusLabel.setText("Opponent's turn.");
        checkWin();
    }

    private void sendMove(int row, int col) {
        if (out != null) {
            out.println(row + "," + col);
        }
    }

    private void hostGame() {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            // Accept one client connection
            socket = serverSocket.accept();
            Platform.runLater(() -> statusLabel.setText("Opponent connected. Your turn."));
            setupStreams();
            listenForMoves();
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> statusLabel.setText("Error hosting game."));
        }
    }

    private void joinGame(String hostIP) {
        try {
            socket = new Socket(hostIP, 55555);
            Platform.runLater(() -> statusLabel.setText("Connected to host. Opponent's turn."));
            setupStreams();
            listenForMoves();
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> statusLabel.setText("Error connecting to host."));
        }
    }

    private void setupStreams() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    private void listenForMoves() {
        new Thread(() -> {
            String input;
            try {
                while ((input = in.readLine()) != null) {
                    String[] parts = input.split(",");
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);

                    Platform.runLater(() -> {
                        buttons[row][col].setText(String.valueOf(opponentMark));
                        checkWin();
                        myTurn = true;
                        statusLabel.setText("Your turn.");
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> statusLabel.setText("Connection lost."));
            }
        }).start();
    }

    private void checkWin() {
        // Check if current player or opponent won
        if (hasPlayerWon(myMark)) {
            statusLabel.setText("You win!");
            disableAllButtons();
            closeConnection();
        } else if (hasPlayerWon(opponentMark)) {
            statusLabel.setText("Opponent wins!");
            disableAllButtons();
            closeConnection();
        } else if (isBoardFull()) {
            statusLabel.setText("It's a draw!");
            closeConnection();
        }
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

    private void disableAllButtons() {
        for (Button[] row : buttons) {
            for (Button btn : row) {
                btn.setDisable(true);
            }
        }
    }

    private void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
