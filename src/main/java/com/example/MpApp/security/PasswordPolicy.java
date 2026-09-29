package com.example.MpApp.security;

import java.util.UUID;

public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;

    /**
     * The single definition of "acceptable password": at least {@link #MIN_LENGTH}
     * characters, with at least one uppercase letter, one lowercase letter, one
     * digit and one non-alphanumeric character.
     *
     * <p>This exists so the DTO layer and {@link #validate(String)} cannot drift
     * apart. {@code ForgotPasswordRequest} and {@code ChangePasswordRequest} cite
     * this exact constant in their {@code @Pattern}, so a request is rejected at
     * the boundary with a message the client can display, instead of passing
     * validation and then failing deep inside a service method. It must remain a
     * compile-time constant for {@code @Pattern} to accept it.
     *
     * <p>{@code [\\s\\S]} is used instead of {@code .} so that {@link #validate}
     * and this pattern agree even for input containing a line break.
     */
    public static final String PATTERN =
            "^(?=[\\s\\S]{" + MIN_LENGTH + ",})(?=[\\s\\S]*[A-Z])(?=[\\s\\S]*[a-z])"
                    + "(?=[\\s\\S]*\\d)(?=[\\s\\S]*[^A-Za-z0-9])[\\s\\S]+$";

    /** Human-readable summary, used in validation messages. */
    public static final String RULE_DESCRIPTION =
            "Password must be at least " + MIN_LENGTH
                    + " characters and include an uppercase letter, a lowercase letter,"
                    + " a number and a special character";

    private PasswordPolicy() {
    }

    public static String generateTemporaryPassword() {
        String value = UUID.randomUUID().toString().replace("-", "");
        return "Tmp@" + value.substring(0, 10) + "9A";
    }

    public static boolean isStrong(String password) {
        return password != null && password.matches(PATTERN);
    }

    /**
     * Authority for the password rules. Kept as individual checks rather than a
     * single {@code matches(PATTERN)} so the caller gets the specific reason it
     * failed instead of one generic message.
     */
    public static void validate(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_LENGTH + " characters long");
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
