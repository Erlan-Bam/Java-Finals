package com.example.javafinals;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class DatabaseController {

    private static final String URL = "jdbc:postgresql://junction.proxy.rlwy.net:15131/railway";
    private static final String USER = "postgres";
    private static final String PASSWORD = "cdZGLCHAEZlYZWaEZnIrGZtiPZXqWVTm";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean registerUser(String username, String email, String password) throws Exception {
        String checkUserQuery = "SELECT * FROM users WHERE username = ? OR email = ?";
        String insertUserQuery = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkUserQuery)) {

            checkStmt.setString(1, username);
            checkStmt.setString(2, email);

            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return false; // User already exists
            }

            String hashedPassword = hashPassword(password);

            try (PreparedStatement insertStmt = conn.prepareStatement(insertUserQuery)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, email);
                insertStmt.setString(3, hashedPassword);

                insertStmt.executeUpdate();
                return true;
            }
        }
    }

    public static boolean authenticateUser(String username, String password) throws Exception {
        String query = "SELECT password FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                return verifyPassword(password, storedHashedPassword);
            } else {
                return false; // User not found
            }
        }
    }

    private static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(password.getBytes());
        return bytesToHex(hashedBytes);
    }

    private static boolean verifyPassword(String password, String storedHash) throws NoSuchAlgorithmException {
        String hashedPassword = hashPassword(password);
        return hashedPassword.equals(storedHash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for(byte b : bytes){
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static void storeMessage(String sender, String receiver, String message) throws Exception {
        String insertMessageQuery = "INSERT INTO messages (sender_username, receiver_username, message) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertMessageQuery)) {

            insertStmt.setString(1, sender);
            insertStmt.setString(2, receiver);
            insertStmt.setString(3, message);

            insertStmt.executeUpdate();
        }
    }

    public static ResultSet getChatHistory(String user1, String user2) throws Exception {
        String getHistoryQuery = "SELECT sender_username, receiver_username, message, timestamp FROM messages " +
                "WHERE (sender_username = ? AND receiver_username = ?) " +
                "OR (sender_username = ? AND receiver_username = ?) " +
                "ORDER BY timestamp ASC";

        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(getHistoryQuery);
        stmt.setString(1, user1);
        stmt.setString(2, user2);
        stmt.setString(3, user2);
        stmt.setString(4, user1);

        return stmt.executeQuery();
    }
}
