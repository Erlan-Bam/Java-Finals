package com.example.javafinals;

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
                return false;
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
                return false;
            }
        }
    }

    private static String hashPassword(String password) {
        return password;
    }

    private static boolean verifyPassword(String password, String storedHash) {
        return password.equals(storedHash);
    }
}
