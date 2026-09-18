package com.example.MpApp.service.payment;

import com.example.MpApp.dto.payment.CreateRazorpayOrderRequest;
import com.example.MpApp.dto.payment.VerifyRazorpayPaymentRequest;
import com.example.MpApp.entity.course.Course;
import com.example.MpApp.entity.course.StudentCourseRegistration;
import com.example.MpApp.entity.payment.RazorpayPayment;
import com.example.MpApp.exception.ResourceNotFoundException;
import com.example.MpApp.repository.course.CourseRepository;
import com.example.MpApp.repository.course.StudentCourseRegistrationRepository;
import com.example.MpApp.repository.payment.RazorpayPaymentRepository;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RazorpayPaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;


    @Autowired
    private RazorpayPaymentRepository paymentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentCourseRegistrationRepository registrationRepository;


    /*
    ==========================================================
    CREATE RAZORPAY ORDER
    ==========================================================
    */

    @Transactional
    public Map<String, Object> createOrder(
            String token,
            CreateRazorpayOrderRequest request) {

        if (request.getRegistrationId() == null) {

            throw new IllegalArgumentException(
                    "Registration ID is required"
            );
        }


        StudentCourseRegistration registration =
                registrationRepository
                        .findById(request.getRegistrationId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Registration Not Found"
                                )
                        );


        /*
        ======================================================
        SECURITY CHECK
        ======================================================

        Only create payment for pending registration.
        */

        if (!"PENDING_PAYMENT"
                .equalsIgnoreCase(
                        registration.getRegistrationStatus())) {

            throw new IllegalStateException(
                    "This registration is not waiting for payment"
            );
        }


        /*
        ======================================================
        GET REGISTRATION FEE
        ======================================================
        */

        Double registrationFee =
                registration.getRegistrationFeeAmount();

        if (registrationFee == null ||
                registrationFee <= 0) {

            throw new IllegalStateException(
                    "Registration fee is not available"
            );
        }


        /*
        ======================================================
        CONVERT RUPEES TO PAISE
        ======================================================
        */

        long amountInPaise =
                Math.round(registrationFee * 100);


        try {

            RazorpayClient razorpayClient =
                    new RazorpayClient(
                            keyId,
                            keySecret
                    );


            /*
            ==================================================
            CREATE RAZORPAY ORDER
            ==================================================
            */

            JSONObject orderRequest =
                    new JSONObject();

            orderRequest.put(
                    "amount",
                    amountInPaise
            );

            orderRequest.put(
                    "currency",
                    "INR"
            );

            orderRequest.put(
                    "receipt",
                    "REG-" +
                            registration.getId()
            );


            /*
            ==================================================
            ADD NOTES
            ==================================================
            */

            JSONObject notes =
                    new JSONObject();

            notes.put(
                    "registrationId",
                    registration.getId()
            );

            notes.put(
                    "courseId",
                    registration
                            .getCourse()
                            .getId()
            );

            notes.put(
                    "mode",
                    registration.getMode()
            );

            if (registration.getLocation() != null) {

                notes.put(
                        "location",
                        registration.getLocation()
                );
            }

            orderRequest.put(
                    "notes",
                    notes
            );


            Order order =
                    razorpayClient.orders.create(
                            orderRequest
                    );


            String razorpayOrderId =
                    order.get("id");


            /*
            ==================================================
            SAVE PAYMENT RECORD
            ==================================================
            */

            RazorpayPayment payment =
                    new RazorpayPayment();

            payment.setRegistration(
                    registration
            );

            payment.setRazorpayOrderId(
                    razorpayOrderId
            );

            payment.setAmount(
                    amountInPaise
            );

            payment.setCurrency(
                    "INR"
            );

            payment.setPaymentStatus(
                    "PAYMENT_PENDING"
            );

            paymentRepository.save(payment);


            /*
            ==================================================
            RESPONSE TO FRONTEND
            ==================================================
            */

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "registrationId",
                    registration.getId()
            );

            response.put(
                    "razorpayOrderId",
                    razorpayOrderId
            );

            response.put(
                    "amount",
                    amountInPaise
            );

            response.put(
                    "currency",
                    "INR"
            );

            response.put(
                    "keyId",
                    keyId
            );

            response.put(
                    "message",
                    "Razorpay order created successfully"
            );

            return response;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to create Razorpay order: "
                            + e.getMessage(),
                    e
            );
        }
    }


    /*
    ==========================================================
    VERIFY RAZORPAY PAYMENT
    ==========================================================
    */

    @Transactional
    public Map<String, Object> verifyPayment(
            VerifyRazorpayPaymentRequest request) {

        if (request.getRegistrationId() == null) {

            throw new IllegalArgumentException(
                    "Registration ID is required"
            );
        }


        if (request.getRazorpayPaymentId() == null ||
                request.getRazorpayPaymentId().isBlank()) {

            throw new IllegalArgumentException(
                    "Razorpay Payment ID is required"
            );
        }


        if (request.getRazorpayOrderId() == null ||
                request.getRazorpayOrderId().isBlank()) {

            throw new IllegalArgumentException(
                    "Razorpay Order ID is required"
            );
        }


        if (request.getRazorpaySignature() == null ||
                request.getRazorpaySignature().isBlank()) {

            throw new IllegalArgumentException(
                    "Razorpay Signature is required"
            );
        }


        /*
        ======================================================
        GET REGISTRATION
        ======================================================
        */

        StudentCourseRegistration registration =
                registrationRepository
                        .findById(
                                request.getRegistrationId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Registration Not Found"
                                )
                        );


        /*
        ======================================================
        GET OUR PAYMENT RECORD
        ======================================================
        */

        RazorpayPayment payment =
                paymentRepository
                        .findByRazorpayOrderId(
                                request.getRazorpayOrderId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Razorpay Order Not Found"
                                )
                        );


        /*
        ======================================================
        IMPORTANT SECURITY CHECK
        ======================================================
        */

        if (!payment
                .getRegistration()
                .getId()
                .equals(registration.getId())) {

            throw new IllegalArgumentException(
                    "Payment does not belong to this registration"
            );
        }


        /*
        ======================================================
        PREVENT DUPLICATE PAYMENT PROCESSING
        ======================================================
        */

        if ("PAID".equalsIgnoreCase(
                payment.getPaymentStatus())) {

            return Map.of(
                    "success", true,
                    "message",
                    "Payment already verified",
                    "registrationId",
                    registration.getId()
            );
        }


        /*
        ======================================================
        VERIFY ORDER ID
        ======================================================
        */

        if (!payment
                .getRazorpayOrderId()
                .equals(request.getRazorpayOrderId())) {

            throw new IllegalArgumentException(
                    "Invalid Razorpay Order ID"
            );
        }


        /*
        ======================================================
        VERIFY SIGNATURE
        ======================================================

        Razorpay requires:

        HMAC_SHA256(
            orderId + "|" + paymentId,
            keySecret
        )

        ======================================================
        */

        try {

            JSONObject options =
                    new JSONObject();

            options.put(
                    "razorpay_order_id",
                    payment.getRazorpayOrderId()
            );

            options.put(
                    "razorpay_payment_id",
                    request.getRazorpayPaymentId()
            );

            options.put(
                    "razorpay_signature",
                    request.getRazorpaySignature()
            );


            boolean verified =
                    Utils.verifyPaymentSignature(
                            options,
                            keySecret
                    );


            if (!verified) {

                payment.setPaymentStatus(
                        "FAILED"
                );

                paymentRepository.save(
                        payment
                );

                throw new IllegalArgumentException(
                        "Invalid Razorpay payment signature"
                );
            }


            /*
            ==================================================
            PAYMENT VERIFIED
            ==================================================
            */

            payment.setRazorpayPaymentId(
                    request.getRazorpayPaymentId()
            );

            payment.setRazorpaySignature(
                    request.getRazorpaySignature()
            );

            payment.setPaymentStatus(
                    "PAID"
            );

            payment.setPaymentDate(
                    LocalDateTime.now()
            );

            paymentRepository.save(
                    payment
            );


            /*
            ==================================================
            CONFIRM REGISTRATION
            ==================================================
            */

            registration.setPaymentStatus(
                    "PAID"
            );

            registration.setRegistrationStatus(
                    "CONFIRMED"
            );

            Long courseId = registration.getCourse().getId();

            Course course = courseRepository.findById(courseId).orElseThrow(() -> new ResourceNotFoundException("Course Not Found"));
            String branch = registration.getLocation();
            String mode = registration.getMode();

            if(mode.equalsIgnoreCase("ONLINE")) {
                course.setAvailableSeatsOnline(course.getAvailableSeatsOnline() - 1);
            }

            if(mode.equalsIgnoreCase("OFFLINE")) {
                if(branch.equalsIgnoreCase("Tirunelveli")) {
                    course.setAvailableSeatsTirunelveli(course.getAvailableSeatsTirunelveli() - 1);
                } else if(branch.equalsIgnoreCase("Tisaiyanvilai")) {
                    course.setAvailableSeatsTisaiyanvilai(course.getAvailableSeatsTisaiyanvilai() - 1);
                }
            }

            courseRepository.save(course);







            registrationRepository.save(
                    registration
            );


            /*
            ==================================================
            RESPONSE
            ==================================================
            */

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    true
            );

            response.put(
                    "registrationId",
                    registration.getId()
            );

            response.put(
                    "paymentId",
                    payment.getRazorpayPaymentId()
            );

            response.put(
                    "orderId",
                    payment.getRazorpayOrderId()
            );

            response.put(
                    "amount",
                    payment.getAmount()
            );

            response.put(
                    "paymentStatus",
                    "PAID"
            );

            response.put(
                    "registrationStatus",
                    "CONFIRMED"
            );

            response.put(
                    "message",
                    "Payment verified and registration confirmed"
            );

            return response;

        } catch (IllegalArgumentException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Payment verification failed: "
                            + e.getMessage(),
                    e
            );
        }
    }


    /*
    ==========================================================
    GET PAYMENT BY ID
    ==========================================================
    */

    public RazorpayPayment getPaymentById(
            Long id) {

        return paymentRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment Not Found"
                        )
                );
    }


    /*
    ==========================================================
    GET PAYMENTS FOR REGISTRATION
    ==========================================================
    */

    public List<RazorpayPayment>
    getPaymentsByRegistration(
            Long registrationId) {

        return paymentRepository
                .findByRegistrationId(
                        registrationId
                );
    }
}