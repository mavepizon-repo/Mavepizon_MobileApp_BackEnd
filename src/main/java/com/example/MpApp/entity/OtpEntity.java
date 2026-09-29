package com.example.MpApp.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "otps")
@Data
public class OtpEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    /**
     * Account type this OTP belongs to. The same address can exist in more than
     * one role table, so lookups are always scoped by (email, role) and never by
     * email alone.
     */
    @Column(nullable = false, length = 50)
    private String role;

    private String otpCode;
    private LocalDateTime expiryTime;

    @Column(nullable = false)
    private int verificationAttempts = 0;

    private LocalDateTime lastSentAt;

    /**
     * Set once the correct code has been presented. The row is kept after
     * verification so the reset step can consume it exactly once.
     */
    @Column(nullable = false)
    private boolean verified = false;

    private LocalDateTime verifiedAt;
}
