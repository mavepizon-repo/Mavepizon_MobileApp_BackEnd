package com.example.MpApp.dto.common;

import com.example.MpApp.security.PasswordPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    /**
     * The target account is always derived from the authenticated principal in
     * the security context. It is intentionally absent from this payload so a
     * caller cannot target somebody else's account.
     */

    @NotBlank(message = "Old password is required")
    private String oldPassword;

    /**
     * Same reasoning as {@code ForgotPasswordRequest}: bound to the shared
     * pattern so the client learns the rules from one place and never reaches
     * the service layer to be rejected by {@code PasswordPolicy.validate}.
     */
    @NotBlank(message = "New password is required")
    @Pattern(regexp = PasswordPolicy.PATTERN, message = PasswordPolicy.RULE_DESCRIPTION)
    private String newPassword;
}
