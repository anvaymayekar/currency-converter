package com.server;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;

public class AuthService {

    // Generate a random salt (16 bytes)
    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return bytesToHex(salt);
    }

    // Hash password with SHA-256 + salt
    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((password + salt).getBytes());
            return bytesToHex(md.digest());
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // Helper: convert bytes → hex
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // Register a new user
    public static RegistrationStatus registerUser(String username, String password) {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        String sql = "INSERT INTO users(username, password_hash, salt) VALUES(?,?,?)";

        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hash);
            pstmt.setString(3, salt);
            pstmt.executeUpdate();
            return RegistrationStatus.SUCCESS;

        } catch (SQLException e) {
            if (e.getMessage().toLowerCase().contains("unique")) {
                return RegistrationStatus.USER_ALREADY_EXISTS;
            } else {
                return RegistrationStatus.FAILURE;
            }
        }
    }

    // Login user
    public static boolean loginUser(String username, String password) {
        String sql = "SELECT password_hash, salt FROM users WHERE username = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");

                String computedHash = hashPassword(password, salt);
                return storedHash.equals(computedHash);
            }

        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
        return false;
    }
}
