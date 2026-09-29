package com.example.MpApp.dto.common;

import com.example.MpApp.security.PasswordPolicy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be exactly 6 digits")
    private String otp;

    /**
     * Bound to {@link PasswordPolicy#PATTERN} rather than restating the rules.
     *
     * <p>This used to be only {@code @Size(min = 8)}, so a password of
     * {@code mypassword} passed DTO validation and was then rejected by
     * {@code PasswordPolicy.validate} inside the service -- after the OTP had
     * already been consumed, forcing the user to restart the entire flow. The
     * pattern here rejects it at the boundary instead, while the OTP is still
     * intact, and the message is the same one the client already knows.
     */
    @NotBlank(message = "New password is required")
    @Pattern(regexp = PasswordPolicy.PATTERN, message = PasswordPolicy.RULE_DESCRIPTION)
    private String newPassword;
}
