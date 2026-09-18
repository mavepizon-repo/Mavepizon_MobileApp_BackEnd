package com.example.MpApp.controller.payment;

import com.example.MpApp.dto.payment.CreateRazorpayOrderRequest;
import com.example.MpApp.dto.payment.VerifyRazorpayPaymentRequest;
import com.example.MpApp.entity.payment.RazorpayPayment;
import com.example.MpApp.service.payment.RazorpayPaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

            @RequestBody
            CreateRazorpayOrderRequest request) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Token Required");
        }


        String token =
                authHeader.substring(7);


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

            @RequestBody
            VerifyRazorpayPaymentRequest request
    ) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Token Required");
        }


        return ResponseEntity.ok(
                service.verifyPayment(request)
        );
    }


    /*
    ==========================================================
    GET PAYMENT BY ID
    ==========================================================
    */

    @GetMapping("/{id}")
    public ResponseEntity<RazorpayPayment>
    getPaymentById(

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,

            @PathVariable Long id) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }


        return ResponseEntity.ok(
                service.getPaymentById(id)
        );
    }


    /*
    ==========================================================
    GET PAYMENTS FOR REGISTRATION
    ==========================================================
    */

    @GetMapping("/registration/{registrationId}")
    public ResponseEntity<List<RazorpayPayment>>
    getPaymentsByRegistration(

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,

            @PathVariable Long registrationId) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }


        return ResponseEntity.ok(
                service.getPaymentsByRegistration(
                        registrationId
                )
        );
    }
}