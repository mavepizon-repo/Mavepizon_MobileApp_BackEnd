package com.example.MpApp.repository.payment;

import com.example.MpApp.entity.payment.RazorpayPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RazorpayPaymentRepository
        extends JpaRepository<RazorpayPayment, Long> {

    Optional<RazorpayPayment>
    findByRazorpayOrderId(String razorpayOrderId);

    Optional<RazorpayPayment>
    findByRazorpayPaymentId(String razorpayPaymentId);

    List<RazorpayPayment>
    findByRegistrationId(Long registrationId);

    Optional<RazorpayPayment>
    findTopByRegistrationIdOrderByIdDesc(
            Long registrationId
    );

    boolean existsByRazorpayPaymentId(
            String razorpayPaymentId
    );
}