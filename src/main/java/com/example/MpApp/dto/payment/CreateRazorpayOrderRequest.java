package com.example.MpApp.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateRazorpayOrderRequest {

    @NotNull(message = "Registration ID is required")
    @Positive(message = "Registration ID must be positive")
    private Long registrationId;

    public CreateRazorpayOrderRequest() {}
    public Long getRegistrationId() { return registrationId; }
    public void setRegistrationId(Long registrationId) { this.registrationId = registrationId; }
}
