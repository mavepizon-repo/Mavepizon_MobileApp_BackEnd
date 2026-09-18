package com.example.MpApp.entity.payment;

import com.example.MpApp.entity.course.StudentCourseRegistration;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "razorpay_payments",
        indexes = {
                @Index(
                        name = "idx_razorpay_order_id",
                        columnList = "razorpay_order_id"
                ),
                @Index(
                        name = "idx_razorpay_payment_id",
                        columnList = "razorpay_payment_id"
                )
        }
)
public class RazorpayPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
    ==========================================
    REGISTRATION
    ==========================================
    */

    @JsonIgnoreProperties({
            "student",
            "course",
            "hibernateLazyInitializer",
            "handler"
    })
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "registration_id",
            nullable = false
    )
    private StudentCourseRegistration registration;


    /*
    ==========================================
    RAZORPAY DETAILS
    ==========================================
    */

    @Column(
            name = "razorpay_order_id",
            nullable = false,
            unique = true
    )
    private String razorpayOrderId;

    @Column(
            name = "razorpay_payment_id",
            unique = true
    )
    private String razorpayPaymentId;

    @Column(
            name = "razorpay_signature",
            length = 500
    )
    private String razorpaySignature;


    /*
    ==========================================
    PAYMENT DETAILS
    ==========================================
    */

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private String currency;

    /*
     CREATED
     PAYMENT_PENDING
     PAID
     FAILED
     REFUNDED
    */
    @Column(nullable = false)
    private String paymentStatus;


    /*
    ==========================================
    DATE / TIME
    ==========================================
    */

    private LocalDateTime paymentDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    /*
    ==========================================
    CONSTRUCTOR
    ==========================================
    */

    public RazorpayPayment() {
    }


    /*
    ==========================================
    PRE PERSIST
    ==========================================
    */

    @PrePersist
    public void prePersist() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        updatedAt = LocalDateTime.now();

        if (paymentStatus == null) {
            paymentStatus = "PAYMENT_PENDING";
        }

        if (currency == null) {
            currency = "INR";
        }
    }


    /*
    ==========================================
    PRE UPDATE
    ==========================================
    */

    @PreUpdate
    public void preUpdate() {

        updatedAt = LocalDateTime.now();
    }


    /*
    ==========================================
    GETTERS & SETTERS
    ==========================================
    */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StudentCourseRegistration getRegistration() {
        return registration;
    }

    public void setRegistration(
            StudentCourseRegistration registration) {

        this.registration = registration;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(
            String razorpayOrderId) {

        this.razorpayOrderId = razorpayOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(
            String razorpayPaymentId) {

        this.razorpayPaymentId = razorpayPaymentId;
    }

    public String getRazorpaySignature() {
        return razorpaySignature;
    }

    public void setRazorpaySignature(
            String razorpaySignature) {

        this.razorpaySignature = razorpaySignature;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(
            LocalDateTime paymentDate) {

        this.paymentDate = paymentDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt) {

        this.updatedAt = updatedAt;
    }
}