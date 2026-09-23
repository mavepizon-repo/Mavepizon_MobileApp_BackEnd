package com.example.MpApp.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class VerifyRazorpayPaymentRequest {

    @NotNull(message = "Registration ID is required")
    @Positive(message = "Registration ID must be positive")
    private Long registrationId;

    @NotBlank(message = "Razorpay payment ID is required")
    @Size(max = 100, message = "Razorpay payment ID is too long")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay order ID is required")
    @Size(max = 100, message = "Razorpay order ID is too long")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay signature is required")
    @Size(max = 200, message = "Razorpay signature is too long")
    private String razorpaySignature;

    public VerifyRazorpayPaymentRequest() {}
    public Long getRegistrationId() { return registrationId; }
    public void setRegistrationId(Long registrationId) { this.registrationId = registrationId; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }
    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }
    public String getRazorpaySignature() { return razorpaySignature; }
    public void setRazorpaySignature(String razorpaySignature) { this.razorpaySignature = razorpaySignature; }
}
