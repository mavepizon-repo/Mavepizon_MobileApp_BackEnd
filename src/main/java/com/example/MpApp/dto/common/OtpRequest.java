package com.example.MpApp.dto.common;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Body for OTP issue and re-issue requests. Travels as JSON so the address is
 * not left in access logs, browser history or proxy records.
 */
@Data
public class OtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;
}
