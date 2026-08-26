package com.example.MpApp.service.course;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.dto.course.StudentCourseRegistrationRequest;
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

@Service
public class StudentCourseRegistrationService {

    @Autowired
    private StudentCourseRegistrationRepository repository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private JwtService jwtService;


    // =========================================================
    // REGISTER COURSE
    // =========================================================

    @Transactional
    public Map<String, Object> registerCourse(
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
        // 2. VALIDATE COURSE ID
        // =====================================================

        if (request.getCourseId() == null) {

            throw new IllegalArgumentException(
                    "Course ID is required"
            );
        }


        // =====================================================
        // 3. FIND COURSE
        // =====================================================

        Course course =
                courseRepository
                        .findById(request.getCourseId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Course Not Found"
                                )
                        );


        // =====================================================
        // 4. CHECK COURSE STATUS
        // =====================================================

        if (course.getStatus() == null ||
                !"ACTIVE".equalsIgnoreCase(
                        course.getStatus()
                )) {

            throw new IllegalStateException(
                    "Course registration is currently closed"
            );
        }


        // =====================================================
        // 5. CHECK REGISTRATION DATE
        // =====================================================

        LocalDate today = LocalDate.now();


        if (course.getRegistrationStartDate() != null &&
                today.isBefore(
                        course.getRegistrationStartDate()
                )) {

            throw new IllegalStateException(
                    "Course registration has not started yet"
            );
        }


        if (course.getRegistrationEndDate() != null &&
                today.isAfter(
                        course.getRegistrationEndDate()
                )) {

            throw new IllegalStateException(
                    "Course registration is closed"
            );
        }


        // =====================================================
        // 6. CHECK DUPLICATE REGISTRATION
        // =====================================================

        boolean alreadyRegistered =
                repository
                        .existsByStudentStudentIdAndCourseId(
                                student.getStudentId(),
                                course.getId()
                        );


        if (alreadyRegistered) {

            StudentCourseRegistration existing =
                    repository
                            .findByStudentStudentIdAndCourseId(
                                    student.getStudentId(),
                                    course.getId()
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
                        "You are already registered for this course"
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
        // 7. VALIDATE MODE
        // =====================================================

        String mode =
                normalizeMode(
                        request.getMode()
                );


        // =====================================================
        // 8. VALIDATE LOCATION
        // =====================================================

        String location =
                normalizeLocation(
                        mode,
                        request.getLocation()
                );


        // =====================================================
        // 9. CHECK SEAT AVAILABILITY
        // =====================================================

        checkSeatAvailability(
                course,
                mode,
                location
        );


        // =====================================================
        // 10. CREATE REGISTRATION
        // =====================================================

        StudentCourseRegistration registration =
                new StudentCourseRegistration();


        registration.setStudent(student);

        registration.setCourse(course);


        // =====================================================
        // 11. SET MODE
        // =====================================================

        registration.setMode(mode);


        // =====================================================
        // 12. SET LOCATION
        // =====================================================

        registration.setLocation(location);


        // =====================================================
        // 13. VALIDATE COURSE FEE
        // =====================================================

        if (course.getTotalFees() == null ||
                course.getTotalFees() <= 0) {

            throw new IllegalStateException(
                    "Course fee is not configured"
            );
        }


        // =====================================================
        // 14. CALCULATE REGISTRATION FEE
        // =====================================================

        /*
         * Backend calculates 30%.
         *
         * Frontend does NOT send the amount.
         */

        Double registrationFee =
                course.getTotalFees() * 0.30;


        registrationFee =
                Math.round(
                        registrationFee * 100.0
                ) / 100.0;


        registration.setRegistrationFeeAmount(
                registrationFee
        );


        // =====================================================
        // 15. INITIAL PAYMENT STATUS
        // =====================================================

        registration.setPaymentStatus(
                "PENDING"
        );


        // =====================================================
        // 16. INITIAL REGISTRATION STATUS
        // =====================================================

        registration.setRegistrationStatus(
                "PENDING_PAYMENT"
        );


        // =====================================================
        // 17. STUDENT COURSE COUNT
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
        // 18. SAVE REGISTRATION
        // =====================================================

        StudentCourseRegistration saved =
                repository.save(
                        registration
                );


        // =====================================================
        // 19. RESPONSE
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
                "courseId",
                course.getId()
        );


        response.put(
                "courseCode",
                course.getCourseCode()
        );


        response.put(
                "courseName",
                course.getCourseName()
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
                "Registration created. Complete Razorpay payment to confirm your registration."
        );


        return response;
    }


    // =========================================================
    // BUILD PENDING PAYMENT RESPONSE
    // =========================================================

    private Map<String, Object> buildPendingPaymentResponse(
            StudentCourseRegistration registration) {

        Course course =
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
                "courseId",
                course.getId()
        );


        response.put(
                "courseCode",
                course.getCourseCode()
        );


        response.put(
                "courseName",
                course.getCourseName()
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
            Course course,
            String mode,
            String location) {


        // -----------------------------------------------------
        // ONLINE
        // -----------------------------------------------------

        if ("ONLINE".equals(mode)) {

            Integer available =
                    course.getAvailableSeatsOnline();


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
                    course.getAvailableSeatsOffline();


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
                        course.getAvailableSeatsTirunelveli();


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
                        course.getAvailableSeatsTisaiyanvilai();


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
        // 2. ALREADY CONFIRMED
        // =====================================================

        if ("CONFIRMED".equalsIgnoreCase(
                registration.getRegistrationStatus()
        )) {

            return buildConfirmationResponse(
                    registration
            );
        }


        // =====================================================
        // 3. GET COURSE
        // =====================================================

        Course course =
                registration.getCourse();


        if (course == null) {

            throw new IllegalStateException(
                    "Course is not linked to registration"
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
                course,
                mode,
                location
        );


        // =====================================================
        // 6. CONSUME SEAT
        // =====================================================

        consumeSeat(
                course,
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
        // 9. SAVE COURSE
        // =====================================================

        courseRepository.save(course);


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
            Course course,
            String mode,
            String location) {


        // -----------------------------------------------------
        // ONLINE
        // -----------------------------------------------------

        if ("ONLINE".equals(mode)) {

            Integer available =
                    course.getAvailableSeatsOnline();

            Integer registered =
                    course.getRegisteredSeatsOnline();


            if (available == null) {
                available = 0;
            }

            if (registered == null) {
                registered = 0;
            }


            course.setAvailableSeatsOnline(
                    available - 1
            );


            course.setRegisteredSeatsOnline(
                    registered + 1
            );


            return;
        }


        // -----------------------------------------------------
        // OFFLINE
        // -----------------------------------------------------

        if ("OFFLINE".equals(mode)) {

            Integer available =
                    course.getAvailableSeatsOffline();

            Integer registered =
                    course.getRegisteredSeatsOffline();


            if (available == null) {
                available = 0;
            }

            if (registered == null) {
                registered = 0;
            }


            course.setAvailableSeatsOffline(
                    available - 1
            );


            course.setRegisteredSeatsOffline(
                    registered + 1
            );


            // -------------------------------------------------
            // TIRUNELVELI
            // -------------------------------------------------

            if ("TIRUNELVELI".equals(location)) {

                Integer locationAvailable =
                        course.getAvailableSeatsTirunelveli();

                Integer locationRegistered =
                        course.getRegisteredSeatsTirunelveli();


                if (locationAvailable == null) {
                    locationAvailable = 0;
                }

                if (locationRegistered == null) {
                    locationRegistered = 0;
                }


                course.setAvailableSeatsTirunelveli(
                        locationAvailable - 1
                );


                course.setRegisteredSeatsTirunelveli(
                        locationRegistered + 1
                );
            }


            // -------------------------------------------------
            // TISAIYANVILAI
            // -------------------------------------------------

            else if ("TISAIYANVILAI".equals(location)) {

                Integer locationAvailable =
                        course.getAvailableSeatsTisaiyanvilai();

                Integer locationRegistered =
                        course.getRegisteredSeatsTisaiyanvilai();


                if (locationAvailable == null) {
                    locationAvailable = 0;
                }

                if (locationRegistered == null) {
                    locationRegistered = 0;
                }


                course.setAvailableSeatsTisaiyanvilai(
                        locationAvailable - 1
                );


                course.setRegisteredSeatsTisaiyanvilai(
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

        Course course =
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
                "courseId",
                course.getId()
        );


        response.put(
                "courseCode",
                course.getCourseCode()
        );


        response.put(
                "courseName",
                course.getCourseName()
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
                "Registration confirmed successfully"
        );


        return response;
    }


    // =========================================================
    // GET ALL REGISTRATIONS
    // =========================================================

    public List<StudentCourseRegistration>
    getAllRegistrations() {

        return repository
                .findAllWithStudentAndCourse();
    }


    // =========================================================
    // GET REGISTRATION BY ID
    // =========================================================

    public StudentCourseRegistration
    getRegistrationById(Long id) {

        return repository
                .findByIdWithStudentAndCourse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Registration Not Found"
                        )
                );
    }


    // =========================================================
    // GET BY STUDENT
    // =========================================================

    public List<StudentCourseRegistration>
    getByStudentId(String studentId) {

        return repository
                .findByStudentStudentId(
                        studentId
                );
    }


    // =========================================================
    // GET BY COURSE
    // =========================================================

    public List<StudentCourseRegistration>
    getByCourseId(Long courseId) {

        return repository
                .findByCourseId(courseId);
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
    // UPDATE COURSE REGISTRATION MODE / LOCATION
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
                                        "Course registration not found"
                                )
                        );


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
        // 7. GET COURSE
        // =====================================================

        Course course =
                registration.getCourse();

        if (course == null) {

            throw new ResourceNotFoundException(
                    "Course not found for this registration"
            );
        }


        // =====================================================
        // 8. CHECK NEW SEAT AVAILABILITY
        // =====================================================

        /*
         * Pending registrations have not consumed a seat yet.
         *
         * Therefore we only check whether the newly selected
         * mode/location has availability.
         *
         * The actual seat is consumed only after successful
         * Razorpay payment inside
         * confirmRegistrationAfterPayment().
         */

        checkSeatAvailability(
                course,
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
                "Course registration updated successfully."
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
                "courseId",
                course.getId()
        );


        response.put(
                "courseCode",
                course.getCourseCode()
        );


        response.put(
                "courseName",
                course.getCourseName()
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
    // MY COURSES
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
                );
    }
}