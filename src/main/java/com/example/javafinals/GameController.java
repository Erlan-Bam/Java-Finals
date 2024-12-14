package com.example.javafinals;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;

public class GameController {

    @FXML
    private GridPane gameGrid;

    @FXML
    private Label statusLabel;

    @FXML
    private Label ipLabel; // Label to display host's IP

    private Button[][] buttons = new Button[3][3];
    private char myMark;
    private char opponentMark;
    private boolean myTurn;

    // Networking variables
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    // Host IP (if joining)
    private static String hostIP = null;

    // Set the host IP before loading the game
    public static void setHostIP(String ip) {
        hostIP = ip;
    }

    @FXML
    public void initialize() {
        initializeGrid();

        if (hostIP == null) {
            // Host the game
            myMark = 'X';
            opponentMark = 'O';
            myTurn = true;
            statusLabel.setText("Hosting game... Waiting for opponent to connect.");
            displayHostIP();
            new Thread(this::hostGame).start();
        } else {
            myMark = 'O';
            opponentMark = 'X';
            myTurn = false;
            statusLabel.setText("Joining game... Connecting to host.");
            new Thread(() -> joinGame(hostIP)).start();
        }
    }

    private void initializeGrid() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Button btn = new Button("");
                btn.setPrefSize(100, 100);
                btn.setStyle("-fx-font-size:36");
                final int r = row;
                final int c = col;
                btn.setOnAction(e -> handleMove(r, c));
                buttons[row][col] = btn;
                gameGrid.add(btn, col, row);
            }
        }
    }

    private void handleMove(int row, int col) {
        if (!myTurn) {
            return;
        }

        Button btn = buttons[row][col];
        if (!btn.getText().isEmpty()) {
            return;
        }

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
        try (ServerSocket serverSocket = new ServerSocket(55555)) {
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
        for (int row = 0; row < 3; row++) {
            if (buttons[row][0].getText().equals(String.valueOf(player)) &&
                    buttons[row][1].getText().equals(String.valueOf(player)) &&
                    buttons[row][2].getText().equals(String.valueOf(player))) {
                return true;
            }
        }

        for (int col = 0; col < 3; col++) {
            if (buttons[0][col].getText().equals(String.valueOf(player)) &&
                    buttons[1][col].getText().equals(String.valueOf(player)) &&
                    buttons[2][col].getText().equals(String.valueOf(player))) {
                return true;
            }
        }

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

    private void displayHostIP() {
        String ip = getLocalIPAddress();
        if (ip != null) {
            Platform.runLater(() -> ipLabel.setText("Your IP Address: " + ip));
        } else {
            Platform.runLater(() -> ipLabel.setText("Unable to determine IP Address."));
        }
    }

    private String getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : java.util.Collections.list(nets)) {
                if (netint.isUp() && !netint.isLoopback() && !netint.isVirtual()) {
                    Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                    for (InetAddress inetAddress : java.util.Collections.list(inetAddresses)) {
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof java.net.Inet4Address) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
