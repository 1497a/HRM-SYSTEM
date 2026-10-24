package com.hrm.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification
 * Uses SHA-256 (standard Java) - no external dependencies required
 */
public class PasswordUtil {

    private static final String SALT_PREFIX = "$sha256$";
    private PasswordUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Hash a plain text password using SHA-256 with random salt
     * @param plainPassword The plain text password
     * @return The hash in format: $sha256$salt$hash
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] saltBytes = new byte[16];
            random.nextBytes(saltBytes);
            String salt = Base64.getEncoder().encodeToString(saltBytes);
            // Hash password with salt
            String hash = hashWithSalt(plainPassword, salt);
            return SALT_PREFIX + salt + "$" + hash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Verify a plain text password against a hash
     * @param plainPassword The plain text password
     * @param hashedPassword The hash to verify against
     * @return true if the password matches
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            // Handle SHA-256 hashes (starts with $sha256$)
            if (hashedPassword.startsWith(SALT_PREFIX)) {
                String[] parts = hashedPassword.split("\\$");
                if (parts.length != 4) {
                    return false;
                }
                String salt = parts[2];
                String expectedHash = parts[3];
                String actualHash = hashWithSalt(plainPassword, salt);
                return expectedHash.equals(actualHash);
            }
            return plainPassword.equals(hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    private static String hashWithSalt(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String saltedPassword = salt + password;
        byte[] hashBytes = md.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }
}
