package com.example.MpApp.dto.payment;

public class VerifyRazorpayPaymentRequest {

    private Long registrationId;

    private String razorpayPaymentId;

    private String razorpayOrderId;

    private String razorpaySignature;


    public VerifyRazorpayPaymentRequest() {
    }


    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(
            Long registrationId) {

        this.registrationId = registrationId;
    }


    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(
            String razorpayPaymentId) {

        this.razorpayPaymentId =
                razorpayPaymentId;
    }


    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(
            String razorpayOrderId) {

        this.razorpayOrderId =
                razorpayOrderId;
    }


    public String getRazorpaySignature() {
        return razorpaySignature;
    }

    public void setRazorpaySignature(
            String razorpaySignature) {

        this.razorpaySignature =
                razorpaySignature;
    }
}