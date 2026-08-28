package com.example.MpApp.service.course;


import com.example.MpApp.config.JwtService;
import com.example.MpApp.dto.course.StudentCourseRegistrationRequest;
import com.example.MpApp.entity.course.Category;
import com.example.MpApp.entity.course.Course;
import com.example.MpApp.entity.course.StudentCourseRegistration;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.exception.ResourceNotFoundException;
import com.example.MpApp.repository.course.CourseRepository;
import com.example.MpApp.repository.course.StudentCourseRegistrationRepository;
import com.example.MpApp.repository.student.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InternshipRegistrationService {

    @Autowired
    private StudentCourseRegistrationRepository repository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private JwtService jwtService;


    // =========================================================
    // REGISTER INTERNSHIP
    // =========================================================

    @Transactional
    public Map<String, Object> registerInternship(
            String token,
            StudentCourseRegistrationRequest request) {

        // =====================================================
        // 1. GET STUDENT FROM JWT
        // =====================================================

        String email =
                jwtService.extractEmail(token);

        Student student =
                studentRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Student Not Found"
                                )
                        );


        // =====================================================
        // 2. VALIDATE INTERNSHIP ID
        // =====================================================

        if (request.getCourseId() == null) {

            throw new IllegalArgumentException(
                    "Internship ID is required"
            );
        }


        // =====================================================
        // 3. FIND INTERNSHIP
        // =====================================================

        Course internship =
                courseRepository
                        .findById(request.getCourseId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Internship Not Found"
                                )
                        );


        // =====================================================
        // 4. CHECK CATEGORY
        // =====================================================

        if (internship.getCategory() != Category.INTERNSHIP) {

            throw new IllegalArgumentException(
                    "This offering is not an Internship"
            );
        }


        // =====================================================
        // 5. CHECK INTERNSHIP STATUS
        // =====================================================

        if (internship.getStatus() == null ||
                !"ACTIVE".equalsIgnoreCase(
                        internship.getStatus()
                )) {

            throw new IllegalStateException(
                    "Internship registration is currently closed"
            );
        }


        // =====================================================
        // 6. CHECK REGISTRATION DATE
        // =====================================================

        LocalDate today = LocalDate.now();


        if (internship.getRegistrationStartDate() != null &&
                today.isBefore(
                        internship.getRegistrationStartDate()
                )) {

            throw new IllegalStateException(
                    "Internship registration has not started yet"
            );
        }


        if (internship.getRegistrationEndDate() != null &&
                today.isAfter(
                        internship.getRegistrationEndDate()
                )) {

            throw new IllegalStateException(
                    "Internship registration is closed"
            );
        }


        // =====================================================
        // 7. CHECK DUPLICATE REGISTRATION
        // =====================================================

        boolean alreadyRegistered =
                repository
                        .existsByStudentStudentIdAndCourseId(
                                student.getStudentId(),
                                internship.getId()
                        );


        if (alreadyRegistered) {

            StudentCourseRegistration existing =
                    repository
                            .findByStudentStudentIdAndCourseId(
                                    student.getStudentId(),
                                    internship.getId()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Registration Not Found"
                                    )
                            );


            // -------------------------------------------------
            // ALREADY CONFIRMED
            // -------------------------------------------------

            if ("CONFIRMED".equalsIgnoreCase(
                    existing.getRegistrationStatus()
            )) {

                throw new IllegalArgumentException(
                        "You are already registered for this internship"
                );
            }


            // -------------------------------------------------
            // EXISTING PENDING PAYMENT
            // -------------------------------------------------

            return buildPendingPaymentResponse(
                    existing
            );
        }


        // =====================================================
        // 8. VALIDATE MODE
        // =====================================================

        String mode =
                normalizeMode(
                        request.getMode()
                );


        // =====================================================
        // 9. VALIDATE LOCATION
        // =====================================================

        String location =
                normalizeLocation(
                        mode,
                        request.getLocation()
                );


        // =====================================================
        // 10. CHECK SEAT AVAILABILITY
        // =====================================================

        checkSeatAvailability(
                internship,
                mode,
                location
        );


        // =====================================================
        // 11. CREATE REGISTRATION
        // =====================================================

        StudentCourseRegistration registration =
                new StudentCourseRegistration();


        registration.setStudent(student);

        registration.setCourse(internship);


        // =====================================================
        // 12. SET MODE
        // =====================================================

        registration.setMode(mode);


        // =====================================================
        // 13. SET LOCATION
        // =====================================================

        registration.setLocation(location);


        // =====================================================
        // 14. VALIDATE INTERNSHIP FEE
        // =====================================================

        if (internship.getTotalFees() == null ||
                internship.getTotalFees() <= 0) {

            throw new IllegalStateException(
                    "Internship fee is not configured"
            );
        }


        // =====================================================
        // 15. CALCULATE REGISTRATION FEE
        // =====================================================

        /*
         * Backend calculates 10%, same as course flow.
         *
         * Frontend does NOT send the amount.
         */

        Double registrationFee =
                internship.getTotalFees() * 0.10;


        registrationFee =
                Math.round(
                        registrationFee * 100.0
                ) / 100.0;


        registration.setRegistrationFeeAmount(
                registrationFee
        );


        // =====================================================
        // 16. INITIAL PAYMENT STATUS
        // =====================================================

        registration.setPaymentStatus(
                "PENDING"
        );


        // =====================================================
        // 17. INITIAL REGISTRATION STATUS
        // =====================================================

        registration.setRegistrationStatus(
                "PENDING_PAYMENT"
        );


        // =====================================================
        // 18. STUDENT COURSE COUNT
        // =====================================================

        Integer currentCount =
                repository.countByStudentStudentId(
                        student.getStudentId()
                );


        if (currentCount == null) {
            currentCount = 0;
        }


        registration.setRegisteredCoursesCount(
                currentCount + 1
        );


        // =====================================================
        // 19. SAVE REGISTRATION
        // =====================================================

        StudentCourseRegistration saved =
                repository.save(
                        registration
                );


        // =====================================================
        // 20. RESPONSE
        // =====================================================

        Map<String, Object> response =
                new HashMap<>();


        response.put(
                "registrationId",
                saved.getId()
        );


        response.put(
                "studentId",
                student.getStudentId()
        );


        response.put(
                "internshipId",
                internship.getId()
        );


        response.put(
                "internshipCode",
                internship.getCourseCode()
        );


        response.put(
                "internshipName",
                internship.getCourseName()
        );


        response.put(
                "mode",
                mode
        );


        response.put(
                "location",
                location
        );


        response.put(
                "registrationFee",
                registrationFee
        );


        response.put(
                "paymentStatus",
                "PENDING"
        );


        response.put(
                "registrationStatus",
                "PENDING_PAYMENT"
        );


        response.put(
                "message",
                "Registration created. Complete Razorpay payment to confirm your internship registration."
        );


        return response;
    }


    // =========================================================
    // BUILD PENDING PAYMENT RESPONSE
    // =========================================================

    private Map<String, Object> buildPendingPaymentResponse(
            StudentCourseRegistration registration) {

        Course internship =
                registration.getCourse();


        Map<String, Object> response =
                new HashMap<>();


        response.put(
                "registrationId",
                registration.getId()
        );


        response.put(
                "studentId",
                registration
                        .getStudent()
                        .getStudentId()
        );


        response.put(
                "internshipId",
                internship.getId()
        );


        response.put(
                "internshipCode",
                internship.getCourseCode()
        );


        response.put(
                "internshipName",
                internship.getCourseName()
        );


        response.put(
                "mode",
                registration.getMode()
        );


        response.put(
                "location",
                registration.getLocation()
        );


        response.put(
                "registrationFee",
                registration.getRegistrationFeeAmount()
        );


        response.put(
                "paymentStatus",
                registration.getPaymentStatus()
        );


        response.put(
                "registrationStatus",
                registration.getRegistrationStatus()
        );


        response.put(
                "message",
                "You already have a pending registration. Complete the payment."
        );


        return response;
    }


    // =========================================================
    // NORMALIZE MODE
    // =========================================================

    private String normalizeMode(
            String mode) {

        if (mode == null ||
                mode.isBlank()) {

            throw new IllegalArgumentException(
                    "Mode is required"
            );
        }


        String normalized =
                mode.trim().toUpperCase();


        if (!normalized.equals("ONLINE") &&
                !normalized.equals("OFFLINE")) {

            throw new IllegalArgumentException(
                    "Invalid mode. Use ONLINE or OFFLINE"
            );
        }


        return normalized;
    }


    // =========================================================
    // NORMALIZE LOCATION
    // =========================================================

    private String normalizeLocation(
            String mode,
            String location) {


        // -----------------------------------------------------
        // ONLINE
        // -----------------------------------------------------

        if ("ONLINE".equals(mode)) {

            return null;
        }


        // -----------------------------------------------------
        // OFFLINE
        // -----------------------------------------------------

        if (location == null ||
                location.isBlank()) {

            throw new IllegalArgumentException(
                    "Location is required for offline registration"
            );
        }


        String normalized =
                location
                        .trim()
                        .toUpperCase();


        if (!normalized.equals("TIRUNELVELI") &&
                !normalized.equals("TISAIYANVILAI")) {

            throw new IllegalArgumentException(
                    "Invalid location. Use TIRUNELVELI or TISAIYANVILAI"
            );
        }


        return normalized;
    }


    // =========================================================
    // CHECK SEAT AVAILABILITY
    // =========================================================

    private void checkSeatAvailability(
            Course internship,
            String mode,
            String location) {


        // -----------------------------------------------------
        // ONLINE
        // -----------------------------------------------------

        if ("ONLINE".equals(mode)) {

            Integer available =
                    internship.getAvailableSeatsOnline();


            if (available == null ||
                    available <= 0) {

                throw new IllegalStateException(
                        "No online seats available"
                );
            }


            return;
        }


        // -----------------------------------------------------
        // OFFLINE
        // -----------------------------------------------------

        if ("OFFLINE".equals(mode)) {

            Integer available =
                    internship.getAvailableSeatsOffline();


            if (available == null ||
                    available <= 0) {

                throw new IllegalStateException(
                        "No offline seats available"
                );
            }


            // -------------------------------------------------
            // TIRUNELVELI
            // -------------------------------------------------

            if ("TIRUNELVELI".equals(location)) {

                Integer availableTirunelveli =
                        internship.getAvailableSeatsTirunelveli();


                if (availableTirunelveli == null ||
                        availableTirunelveli <= 0) {

                    throw new IllegalStateException(
                            "No Tirunelveli seats available"
                    );
                }
            }


            // -------------------------------------------------
            // TISAIYANVILAI
            // -------------------------------------------------

            if ("TISAIYANVILAI".equals(location)) {

                Integer availableTisaiyanvilai =
                        internship.getAvailableSeatsTisaiyanvilai();


                if (availableTisaiyanvilai == null ||
                        availableTisaiyanvilai <= 0) {

                    throw new IllegalStateException(
                            "No Tisaiyanvilai seats available"
                    );
                }
            }
        }
    }


    // =========================================================
    // CONFIRM REGISTRATION AFTER PAYMENT
    // =========================================================

    /*
     * IMPORTANT:
     *
     * This method must be called only AFTER
     * Razorpay payment signature verification.
     *
     * Do not treat registrationId alone as proof
     * of successful payment.
     */

    @Transactional
    public Map<String, Object>
    confirmRegistrationAfterPayment(
            Long registrationId) {


        // =====================================================
        // 1. GET REGISTRATION
        // =====================================================

        StudentCourseRegistration registration =
                repository
                        .findByIdWithStudentAndCourse(
                                registrationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Registration Not Found"
                                )
                        );


        // =====================================================
        // 2. VERIFY THIS IS AN INTERNSHIP REGISTRATION
        // =====================================================

        Course internship =
                registration.getCourse();

        if (internship == null ||
                internship.getCategory() != Category.INTERNSHIP) {

            throw new IllegalArgumentException(
                    "This registration is not an Internship registration"
            );
        }


        // =====================================================
        // 3. ALREADY CONFIRMED
        // =====================================================

        if ("CONFIRMED".equalsIgnoreCase(
                registration.getRegistrationStatus()
        )) {

            return buildConfirmationResponse(
                    registration
            );
        }


        // =====================================================
        // 4. GET MODE + LOCATION
        // =====================================================

        String mode =
                registration.getMode();

        String location =
                registration.getLocation();


        // =====================================================
        // 5. CHECK SEAT AGAIN
        // =====================================================

        checkSeatAvailability(
                internship,
                mode,
                location
        );


        // =====================================================
        // 6. CONSUME SEAT
        // =====================================================

        consumeSeat(
                internship,
                mode,
                location
        );


        // =====================================================
        // 7. PAYMENT SUCCESS
        // =====================================================

        registration.setPaymentStatus(
                "PAID"
        );


        // =====================================================
        // 8. REGISTRATION CONFIRMED
        // =====================================================

        registration.setRegistrationStatus(
                "CONFIRMED"
        );


        // =====================================================
        // 9. SAVE INTERNSHIP
        // =====================================================

        courseRepository.save(internship);


        // =====================================================
        // 10. SAVE REGISTRATION
        // =====================================================

        repository.save(
                registration
        );


        // =====================================================
        // 11. RESPONSE
        // =====================================================

        return buildConfirmationResponse(
                registration
        );
    }


    // =========================================================
    // CONSUME SEAT
    // =========================================================

    private void consumeSeat(
            Course internship,
            String mode,
            String location) {


        // -----------------------------------------------------
        // ONLINE
        // -----------------------------------------------------

        if ("ONLINE".equals(mode)) {

            Integer available =
                    internship.getAvailableSeatsOnline();

            Integer registered =
                    internship.getRegisteredSeatsOnline();


            if (available == null) {
                available = 0;
            }

            if (registered == null) {
                registered = 0;
            }


            internship.setAvailableSeatsOnline(
                    available - 1
            );


            internship.setRegisteredSeatsOnline(
                    registered + 1
            );


            return;
        }


        // -----------------------------------------------------
        // OFFLINE
        // -----------------------------------------------------

        if ("OFFLINE".equals(mode)) {

            Integer available =
                    internship.getAvailableSeatsOffline();

            Integer registered =
                    internship.getRegisteredSeatsOffline();


            if (available == null) {
                available = 0;
            }

            if (registered == null) {
                registered = 0;
            }


            internship.setAvailableSeatsOffline(
                    available - 1
            );


            internship.setRegisteredSeatsOffline(
                    registered + 1
            );


            // -------------------------------------------------
            // TIRUNELVELI
            // -------------------------------------------------

            if ("TIRUNELVELI".equals(location)) {

                Integer locationAvailable =
                        internship.getAvailableSeatsTirunelveli();

                Integer locationRegistered =
                        internship.getRegisteredSeatsTirunelveli();


                if (locationAvailable == null) {
                    locationAvailable = 0;
                }

                if (locationRegistered == null) {
                    locationRegistered = 0;
                }


                internship.setAvailableSeatsTirunelveli(
                        locationAvailable - 1
                );


                internship.setRegisteredSeatsTirunelveli(
                        locationRegistered + 1
                );
            }


            // -------------------------------------------------
            // TISAIYANVILAI
            // -------------------------------------------------

            else if ("TISAIYANVILAI".equals(location)) {

                Integer locationAvailable =
                        internship.getAvailableSeatsTisaiyanvilai();

                Integer locationRegistered =
                        internship.getRegisteredSeatsTisaiyanvilai();


                if (locationAvailable == null) {
                    locationAvailable = 0;
                }

                if (locationRegistered == null) {
                    locationRegistered = 0;
                }


                internship.setAvailableSeatsTisaiyanvilai(
                        locationAvailable - 1
                );


                internship.setRegisteredSeatsTisaiyanvilai(
                        locationRegistered + 1
                );
            }
        }
    }


    // =========================================================
    // BUILD CONFIRMATION RESPONSE
    // =========================================================

    private Map<String, Object>
    buildConfirmationResponse(
            StudentCourseRegistration registration) {

        Course internship =
                registration.getCourse();


        Map<String, Object> response =
                new HashMap<>();


        response.put(
                "registrationId",
                registration.getId()
        );


        response.put(
                "studentId",
                registration
                        .getStudent()
                        .getStudentId()
        );


        response.put(
                "internshipId",
                internship.getId()
        );


        response.put(
                "internshipCode",
                internship.getCourseCode()
        );


        response.put(
                "internshipName",
                internship.getCourseName()
        );


        response.put(
                "mode",
                registration.getMode()
        );


        response.put(
                "location",
                registration.getLocation()
        );


        response.put(
                "registrationFee",
                registration
                        .getRegistrationFeeAmount()
        );


        response.put(
                "paymentStatus",
                registration.getPaymentStatus()
        );


        response.put(
                "registrationStatus",
                registration.getRegistrationStatus()
        );


        response.put(
                "message",
                "Internship registration confirmed successfully"
        );


        return response;
    }


    // =========================================================
    // GET ALL INTERNSHIP REGISTRATIONS
    // =========================================================

    public List<StudentCourseRegistration>
    getAllRegistrations() {

        return repository
                .findAllWithStudentAndCourse()
                .stream()
                .filter(this::isInternship)
                .collect(Collectors.toList());
    }


    // =========================================================
    // GET REGISTRATION BY ID
    // =========================================================

    public StudentCourseRegistration
    getRegistrationById(Long id) {

        StudentCourseRegistration registration =
                repository
                        .findByIdWithStudentAndCourse(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Registration Not Found"
                                )
                        );

        if (!isInternship(registration)) {

            throw new ResourceNotFoundException(
                    "Registration Not Found"
            );
        }

        return registration;
    }


    // =========================================================
    // GET BY STUDENT
    // =========================================================

    public List<StudentCourseRegistration>
    getByStudentId(String studentId) {

        return repository
                .findByStudentStudentId(
                        studentId
                )
                .stream()
                .filter(this::isInternship)
                .collect(Collectors.toList());
    }


    // =========================================================
    // GET BY INTERNSHIP
    // =========================================================

    public List<StudentCourseRegistration>
    getByInternshipId(Long internshipId) {

        return repository
                .findByCourseId(internshipId)
                .stream()
                .filter(this::isInternship)
                .collect(Collectors.toList());
    }


    // =========================================================
    // IS INTERNSHIP HELPER
    // =========================================================

    private boolean isInternship(
            StudentCourseRegistration registration) {

        return registration.getCourse() != null &&
                registration.getCourse().getCategory()
                        == Category.INTERNSHIP;
    }


    // =========================================================
    // DELETE REGISTRATION
    // =========================================================

    @Transactional
    public void deleteRegistration(Long id) {

        StudentCourseRegistration registration =
                repository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Registration Not Found"
                                )
                        );

        if (!isInternship(registration)) {

            throw new ResourceNotFoundException(
                    "Registration Not Found"
            );
        }


        /*
         * Confirmed registration should not
         * be directly deleted because its
         * seat has already been consumed.
         */

        if ("CONFIRMED".equalsIgnoreCase(
                registration.getRegistrationStatus()
        )) {

            throw new IllegalStateException(
                    "Confirmed registration cannot be deleted directly"
            );
        }


        repository.delete(
                registration
        );
    }


    // =========================================================
    // UPDATE INTERNSHIP REGISTRATION MODE / LOCATION
    // ONLY BEFORE PAYMENT
    // =========================================================

    @Transactional
    public Map<String, Object> updateRegistrationMode(
            String token,
            Long registrationId,
            StudentCourseRegistrationRequest request) {

        // =====================================================
        // 1. GET LOGGED-IN STUDENT FROM JWT
        // =====================================================

        String email =
                jwtService.extractEmail(token);

        Student student =
                studentRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Student Not Found"
                                )
                        );


        // =====================================================
        // 2. FIND REGISTRATION BELONGING TO THIS STUDENT
        // =====================================================

        StudentCourseRegistration registration =
                repository
                        .findByIdAndStudentStudentId(
                                registrationId,
                                student.getStudentId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Internship registration not found"
                                )
                        );

        if (!isInternship(registration)) {

            throw new ResourceNotFoundException(
                    "Internship registration not found"
            );
        }


        // =====================================================
        // 3. PAYMENT CHECK
        // =====================================================

        String paymentStatus =
                registration.getPaymentStatus();

        if ("PAID".equalsIgnoreCase(paymentStatus)
                || "SUCCESS".equalsIgnoreCase(paymentStatus)
                || "COMPLETED".equalsIgnoreCase(paymentStatus)) {

            throw new IllegalStateException(
                    "Mode and location cannot be changed after payment."
            );
        }


        // =====================================================
        // 4. REGISTRATION STATUS CHECK
        // =====================================================

        if (!"PENDING_PAYMENT".equalsIgnoreCase(
                registration.getRegistrationStatus()
        )) {

            throw new IllegalStateException(
                    "Mode and location can only be changed before payment."
            );
        }


        // =====================================================
        // 5. VALIDATE NEW MODE
        // =====================================================

        if (request == null) {

            throw new IllegalArgumentException(
                    "Update request is required"
            );
        }

        String newMode =
                normalizeMode(
                        request.getMode()
                );


        // =====================================================
        // 6. VALIDATE NEW LOCATION
        // =====================================================

        String newLocation =
                normalizeLocation(
                        newMode,
                        request.getLocation()
                );


        // =====================================================
        // 7. GET INTERNSHIP
        // =====================================================

        Course internship =
                registration.getCourse();

        if (internship == null) {

            throw new ResourceNotFoundException(
                    "Internship not found for this registration"
            );
        }


        // =====================================================
        // 8. CHECK NEW SEAT AVAILABILITY
        // =====================================================

        /*
         * Pending registrations have not consumed a seat yet.
         *
         * The actual seat is consumed only after successful
         * Razorpay payment inside
         * confirmRegistrationAfterPayment().
         */

        checkSeatAvailability(
                internship,
                newMode,
                newLocation
        );


        // =====================================================
        // 9. UPDATE MODE
        // =====================================================

        registration.setMode(
                newMode
        );


        // =====================================================
        // 10. UPDATE LOCATION
        // =====================================================

        registration.setLocation(
                newLocation
        );


        // =====================================================
        // 11. SAVE REGISTRATION
        // =====================================================

        repository.save(
                registration
        );


        // =====================================================
        // 12. RESPONSE
        // =====================================================

        Map<String, Object> response =
                new HashMap<>();


        response.put(
                "message",
                "Internship registration updated successfully."
        );


        response.put(
                "registrationId",
                registration.getId()
        );


        response.put(
                "studentId",
                student.getStudentId()
        );


        response.put(
                "internshipId",
                internship.getId()
        );


        response.put(
                "internshipCode",
                internship.getCourseCode()
        );


        response.put(
                "internshipName",
                internship.getCourseName()
        );


        response.put(
                "mode",
                registration.getMode()
        );


        response.put(
                "location",
                registration.getLocation()
        );


        response.put(
                "registrationStatus",
                registration.getRegistrationStatus()
        );


        response.put(
                "paymentStatus",
                registration.getPaymentStatus()
        );


        return response;
    }


    // =========================================================
    // MY INTERNSHIPS
    // =========================================================

    public List<StudentCourseRegistration>
    getMyRegistrations(String token) {

        String email =
                jwtService.extractEmail(token);


        Student student =
                studentRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Student Not Found"
                                )
                        );


        return repository
                .findByStudentStudentId(
                        student.getStudentId()
                )
                .stream()
                .filter(this::isInternship)
                .collect(Collectors.toList());
    }
}
