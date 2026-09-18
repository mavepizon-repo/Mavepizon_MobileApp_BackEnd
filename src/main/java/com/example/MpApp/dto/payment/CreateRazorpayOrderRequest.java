package com.example.MpApp.dto.payment;

public class CreateRazorpayOrderRequest {

    private Long registrationId;

    public CreateRazorpayOrderRequest() {
    }

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }
}