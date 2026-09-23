package com.example.MpApp.controller.payment;

import com.example.MpApp.dto.payment.CreateRazorpayOrderRequest;
import com.example.MpApp.dto.payment.VerifyRazorpayPaymentRequest;
import com.example.MpApp.service.payment.RazorpayPaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment/razorpay")
@CrossOrigin("*")
public class RazorpayPaymentController {

    @Autowired
    private RazorpayPaymentService service;

    /*
    ==========================================================
    CREATE RAZORPAY ORDER
    ==========================================================
    */
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,
            @Valid @RequestBody
            CreateRazorpayOrderRequest request) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return tokenRequiredError();
        }

        String token = authHeader.substring(7);

        return ResponseEntity.ok(
                service.createOrder(
                        token,
                        request
                )
        );
    }

    /*
    ==========================================================
    VERIFY PAYMENT
    ==========================================================
    */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,
            @Valid @RequestBody
            VerifyRazorpayPaymentRequest request) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return tokenRequiredError();
        }

        return ResponseEntity.ok(
                service.verifyPayment(
                        authHeader.substring(7),
                        request
                )
        );
    }

    /*
    ==========================================================
    GET PAYMENT BY ID
    ==========================================================
    */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentById(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,
            @PathVariable Long id) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return tokenRequiredError();
        }

        return ResponseEntity.ok(
                service.getPaymentById(
                        authHeader.substring(7),
                        id
                )
        );
    }

    /*
    ==========================================================
    GET PAYMENTS FOR REGISTRATION
    ==========================================================
    */
    @GetMapping("/registration/{registrationId}")
    public ResponseEntity<?> getPaymentsByRegistration(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,
            @PathVariable Long registrationId) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return tokenRequiredError();
        }

        return ResponseEntity.ok(
                service.getPaymentsByRegistration(
                        authHeader.substring(7),
                        registrationId
                )
        );
    }

    /*
    ==========================================================
    STANDARD TOKEN ERROR
    ==========================================================
    */
    private ResponseEntity<Map<String, Object>> tokenRequiredError() {

        Map<String, Object> error =
                new LinkedHashMap<>();

        error.put("success", false);
        error.put("errorCode", "UNAUTHORIZED");

        error.put(
                "timestamp",
                Instant.now().toString()
        );

        error.put(
                "status",
                HttpStatus.UNAUTHORIZED.value()
        );

        error.put(
                "error",
                "Unauthorized"
        );

        error.put(
                "message",
                "Token Required"
        );

        error.put(
                "path",
                "/api/payment/razorpay"
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }
}