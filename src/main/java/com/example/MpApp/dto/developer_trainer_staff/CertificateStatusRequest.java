package com.example.MpApp.dto.developer_trainer_staff;


import jakarta.validation.constraints.NotNull;

public class CertificateStatusRequest {

    @NotNull
    private Long registrationId;

    @NotNull
    private String status;

    public CertificateStatusRequest() {
    }

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}