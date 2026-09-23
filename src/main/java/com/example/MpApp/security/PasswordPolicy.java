package com.example.MpApp.security;

import java.util.UUID;

public final class PasswordPolicy {

    private PasswordPolicy() {
    }

    public static String generateTemporaryPassword() {
        String value = UUID.randomUUID().toString().replace("-", "");
        return "Tmp@" + value.substring(0, 10) + "9A";
    }

    public static void validate(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one number");
        }
        if (!password.matches(".*[^A-Za-z0-9].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
    }
}
