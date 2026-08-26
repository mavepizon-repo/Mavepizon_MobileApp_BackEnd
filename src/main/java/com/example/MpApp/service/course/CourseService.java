package com.example.MpApp.service.course;

import com.example.MpApp.entity.course.Course;
import com.example.MpApp.exception.ResourceNotFoundException;
import com.example.MpApp.repository.course.CourseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    private CourseRepository repository;


    // =========================================================
    // GENERATE COURSE CODE
    // =========================================================
    /*
     * Format:
     *
     * MPCO1001
     * MPCO1002
     * MPCO1003
     *
     * Course code is generated automatically.
     * Frontend must NOT send courseCode.
     */
    private String generateCourseCode() {

        Optional<Course> lastCourse =
                repository.findTopByCourseCodeStartingWithOrderByCourseCodeDesc("MPCO");

        long nextNumber = 1001;

        if (lastCourse.isPresent()
                && lastCourse.get().getCourseCode() != null) {

            String lastCode =
                    lastCourse.get().getCourseCode();

            try {

                String numberPart =
                        lastCode.substring(4);

                nextNumber =
                        Long.parseLong(numberPart) + 1;

            } catch (NumberFormatException e) {

                throw new RuntimeException(
                        "Invalid existing course code format: " + lastCode
                );
            }
        }

        return "MPCO" + nextNumber;
    }


    // =========================================================
    // GENERATE BATCH ID
    // =========================================================
    /*
     * Format:
     *
     * MAV-FULL-2026-01
     * MAV-PYTHON-2026-01
     * MAV-FLUTTER-2026-01
     *
     * One Course = One complete Batch.
     *
     * Batch ID does NOT depend on:
     *
     * ONLINE
     * TIRUNELVELI
     * TISAIYANVILAI
     *
     * Those are registration/staff assignment categories.
     */
    private String generateBatchId(Course course) {

        if (course.getCourseName() == null
                || course.getCourseName().isBlank()) {

            throw new IllegalArgumentException(
                    "Course Name is required"
            );
        }

        String coursePart =
                course.getCourseName()
                        .trim()
                        .toUpperCase()
                        .replaceAll("[^A-Z0-9]", "");

        if (coursePart.length() > 6) {

            coursePart =
                    coursePart.substring(0, 6);
        }

        int year =
                course.getStartDate() != null
                        ? course.getStartDate().getYear()
                        : LocalDate.now().getYear();

        String prefix =
                "MAV-" +
                        coursePart +
                        "-" +
                        year +
                        "-";

        long count =
                repository.countByBatchIdStartingWith(prefix);

        return prefix +
                String.format(
                        "%02d",
                        count + 1
                );
    }


    // =========================================================
    // CREATE COURSE
    // =========================================================

    @Transactional
    public Map<String, String> createCourse(
            Course course) {

        // -----------------------------------------------------
        // BASIC VALIDATION
        // -----------------------------------------------------

        if (course == null) {

            throw new IllegalArgumentException(
                    "Course data is required"
            );
        }


        // -----------------------------------------------------
        // COURSE NAME
        // -----------------------------------------------------

        if (course.getCourseName() == null
                || course.getCourseName().isBlank()) {

            throw new IllegalArgumentException(
                    "Course Name is required"
            );
        }


        // -----------------------------------------------------
        // COURSE DATES
        // -----------------------------------------------------

        if (course.getStartDate() == null) {

            throw new IllegalArgumentException(
                    "Start Date is required"
            );
        }

        if (course.getEndDate() == null) {

            throw new IllegalArgumentException(
                    "End Date is required"
            );
        }

        if (course.getStartDate()
                .isAfter(course.getEndDate())) {

            throw new IllegalArgumentException(
                    "Start Date cannot be after End Date"
            );
        }


        // -----------------------------------------------------
        // REGISTRATION DATES
        // -----------------------------------------------------

        if (course.getRegistrationStartDate() == null) {

            throw new IllegalArgumentException(
                    "Registration Start Date is required"
            );
        }

        if (course.getRegistrationEndDate() == null) {

            throw new IllegalArgumentException(
                    "Registration End Date is required"
            );
        }

        if (course.getRegistrationStartDate()
                .isAfter(
                        course.getRegistrationEndDate()
                )) {

            throw new IllegalArgumentException(
                    "Registration Start Date cannot be after Registration End Date"
            );
        }

        if (course.getRegistrationEndDate()
                .isAfter(course.getStartDate())) {

            throw new IllegalArgumentException(
                    "Registration End Date cannot be after Course Start Date"
            );
        }


        // -----------------------------------------------------
        // FEES
        // -----------------------------------------------------

        if (course.getTotalFees() == null
                || course.getTotalFees() < 0) {

            throw new IllegalArgumentException(
                    "Valid Total Fee is required"
            );
        }


        /*
         * Registration fee = 30% of total fee.
         *
         * Frontend must NOT calculate this.
         */

        double registrationFee =
                course.getTotalFees() * 0.30;

        course.setRegistrationFees(
                registrationFee
        );


        // -----------------------------------------------------
        // COURSE CODE
        // -----------------------------------------------------

        course.setCourseCode(
                generateCourseCode()
        );


        // -----------------------------------------------------
        // BATCH ID
        // -----------------------------------------------------

        course.setBatchId(
                generateBatchId(course)
        );


        // -----------------------------------------------------
        // SEAT VALIDATION
        // -----------------------------------------------------

        validateSeats(course);


        // -----------------------------------------------------
        // INITIAL REGISTERED COUNTS
        // -----------------------------------------------------

        course.setRegisteredSeatsOnline(0);

        course.setRegisteredSeatsOffline(0);

        course.setRegisteredSeatsTirunelveli(0);

        course.setRegisteredSeatsTisaiyanvilai(0);


        // -----------------------------------------------------
        // INITIAL AVAILABLE COUNTS
        // -----------------------------------------------------

        course.setAvailableSeatsOnline(
                safeValue(
                        course.getTotalSeatsOnline()
                )
        );

        course.setAvailableSeatsOffline(
                safeValue(
                        course.getTotalSeatsOffline()
                )
        );

        course.setAvailableSeatsTirunelveli(
                safeValue(
                        course.getTotalSeatsTirunelveli()
                )
        );

        course.setAvailableSeatsTisaiyanvilai(
                safeValue(
                        course.getTotalSeatsTisaiyanvilai()
                )
        );


        // -----------------------------------------------------
        // STATUS
        // -----------------------------------------------------

        if (course.getStatus() == null
                || course.getStatus().isBlank()) {

            course.setStatus("ACTIVE");

        } else {

            course.setStatus(
                    course.getStatus()
                            .trim()
                            .toUpperCase()
            );
        }


        // -----------------------------------------------------
        // SAVE
        // -----------------------------------------------------

        Course savedCourse =
                repository.save(course);


        // -----------------------------------------------------
        // RESPONSE
        // -----------------------------------------------------

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "courseId",
                savedCourse.getId().toString()
        );

        response.put(
                "courseCode",
                savedCourse.getCourseCode()
        );

        response.put(
                "batchId",
                savedCourse.getBatchId()
        );

        response.put(
                "message",
                "Course Created Successfully"
        );

        return response;
    }


    // =========================================================
    // VALIDATE SEATS
    // =========================================================

    private void validateSeats(Course course) {

        Integer online =
                course.getTotalSeatsOnline();

        Integer offline =
                course.getTotalSeatsOffline();

        Integer tirunelveli =
                course.getTotalSeatsTirunelveli();

        Integer tisaiyanvilai =
                course.getTotalSeatsTisaiyanvilai();


        if (online == null) {
            online = 0;
        }

        if (offline == null) {
            offline = 0;
        }

        if (tirunelveli == null) {
            tirunelveli = 0;
        }

        if (tisaiyanvilai == null) {
            tisaiyanvilai = 0;
        }


        // -----------------------------------------------------
        // NEGATIVE VALIDATION
        // -----------------------------------------------------

        if (online < 0) {

            throw new IllegalArgumentException(
                    "Online seats cannot be negative"
            );
        }

        if (offline < 0) {

            throw new IllegalArgumentException(
                    "Offline seats cannot be negative"
            );
        }

        if (tirunelveli < 0) {

            throw new IllegalArgumentException(
                    "Tirunelveli seats cannot be negative"
            );
        }

        if (tisaiyanvilai < 0) {

            throw new IllegalArgumentException(
                    "Tisaiyanvilai seats cannot be negative"
            );
        }


        // -----------------------------------------------------
        // AT LEAST ONE SEAT
        // -----------------------------------------------------

        if (online == 0
                && offline == 0) {

            throw new IllegalArgumentException(
                    "At least one online or offline seat must be provided"
            );
        }


        // -----------------------------------------------------
        // OFFLINE LOCATION VALIDATION
        // -----------------------------------------------------

        int locationTotal =
                tirunelveli +
                        tisaiyanvilai;

        if (locationTotal > offline) {

            throw new IllegalArgumentException(
                    "Tirunelveli and Tisaiyanvilai seats cannot exceed total offline seats"
            );
        }


        // -----------------------------------------------------
        // NORMALIZED VALUES
        // -----------------------------------------------------

        course.setTotalSeatsOnline(online);

        course.setTotalSeatsOffline(offline);

        course.setTotalSeatsTirunelveli(
                tirunelveli
        );

        course.setTotalSeatsTisaiyanvilai(
                tisaiyanvilai
        );
    }


    // =========================================================
    // SAFE INTEGER VALUE
    // =========================================================

    private int safeValue(Integer value) {

        return value == null
                ? 0
                : value;
    }


    // =========================================================
    // GET ALL COURSES
    // =========================================================

    public List<Course> getAllCourses() {

        return repository.findAll();
    }


    // =========================================================
    // GET BY DATABASE ID
    // =========================================================

    public Course getCourseById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course Not Found"
                        )
                );
    }


    // =========================================================
    // GET BY BATCH ID
    // =========================================================

    public Optional<Course> getCourseByBatchId(
            String batchId) {

        if (batchId == null
                || batchId.isBlank()) {

            throw new IllegalArgumentException(
                    "Batch ID is required"
            );
        }

        return repository.findByBatchId(
                batchId.trim()
        );
    }


    // =========================================================
    // GET BY COURSE CODE
    // =========================================================

    public Optional<Course> getCourseByCourseCode(
            String courseCode) {

        if (courseCode == null
                || courseCode.isBlank()) {

            throw new IllegalArgumentException(
                    "Course Code is required"
            );
        }

        return repository.findByCourseCode(
                courseCode.trim()
        );
    }


    // =========================================================
    // UPDATE COURSE
    // =========================================================
    /*
     * Course code cannot be changed.
     * Batch ID cannot be changed.
     *
     * Trainer and Zoom Link are NOT handled here.
     *
     * Staff assignment is handled by:
     * CourseStaffAssignmentService
     */

    @Transactional
    public Map<String, String> updateCourse(
            Long id,
            Course course) {

        // -----------------------------------------------------
        // FIND EXISTING COURSE
        // -----------------------------------------------------

        Course existing =
                repository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Course Not Found"
                                )
                        );


        // -----------------------------------------------------
        // COURSE CODE
        // -----------------------------------------------------
        /*
         * Intentionally ignored.
         *
         * Example:
         * MPCO1001 remains MPCO1001.
         */


        // -----------------------------------------------------
        // BATCH ID
        // -----------------------------------------------------
        /*
         * Intentionally ignored.
         *
         * Example:
         * MAV-FULL-2026-01 remains unchanged.
         */


        // -----------------------------------------------------
        // COURSE NAME
        // -----------------------------------------------------

        if (course.getCourseName() != null
                && !course.getCourseName().isBlank()) {

            existing.setCourseName(
                    course.getCourseName()
            );
        }


        // -----------------------------------------------------
        // DESCRIPTION
        // -----------------------------------------------------

        if (course.getDescription() != null) {

            existing.setDescription(
                    course.getDescription()
            );
        }


        // -----------------------------------------------------
        // DURATION
        // -----------------------------------------------------

        if (course.getDuration() != null) {

            existing.setDuration(
                    course.getDuration()
            );
        }


        // -----------------------------------------------------
        // COURSE DATES
        // -----------------------------------------------------

        if (course.getStartDate() != null) {

            existing.setStartDate(
                    course.getStartDate()
            );
        }

        if (course.getEndDate() != null) {

            existing.setEndDate(
                    course.getEndDate()
            );
        }


        // -----------------------------------------------------
        // REGISTRATION DATES
        // -----------------------------------------------------

        if (course.getRegistrationStartDate() != null) {

            existing.setRegistrationStartDate(
                    course.getRegistrationStartDate()
            );
        }

        if (course.getRegistrationEndDate() != null) {

            existing.setRegistrationEndDate(
                    course.getRegistrationEndDate()
            );
        }


        // -----------------------------------------------------
        // VALIDATE DATES
        // -----------------------------------------------------

        validateUpdatedDates(existing);


        // -----------------------------------------------------
        // FEES
        // -----------------------------------------------------

        if (course.getTotalFees() != null) {

            if (course.getTotalFees() < 0) {

                throw new IllegalArgumentException(
                        "Total Fee cannot be negative"
                );
            }

            existing.setTotalFees(
                    course.getTotalFees()
            );

            existing.setRegistrationFees(
                    course.getTotalFees() * 0.30
            );
        }


        // -----------------------------------------------------
        // ONLINE SEATS
        // -----------------------------------------------------

        if (course.getTotalSeatsOnline() != null) {

            int registered =
                    safeValue(
                            existing
                                    .getRegisteredSeatsOnline()
                    );

            int newTotal =
                    course.getTotalSeatsOnline();

            if (newTotal < registered) {

                throw new IllegalArgumentException(
                        "Online total seats cannot be less than already registered seats"
                );
            }

            existing.setTotalSeatsOnline(
                    newTotal
            );

            existing.setAvailableSeatsOnline(
                    newTotal - registered
            );
        }


        // -----------------------------------------------------
        // OFFLINE SEATS
        // -----------------------------------------------------

        if (course.getTotalSeatsOffline() != null) {

            int registered =
                    safeValue(
                            existing
                                    .getRegisteredSeatsOffline()
                    );

            int newTotal =
                    course.getTotalSeatsOffline();

            if (newTotal < registered) {

                throw new IllegalArgumentException(
                        "Offline total seats cannot be less than already registered seats"
                );
            }

            existing.setTotalSeatsOffline(
                    newTotal
            );

            existing.setAvailableSeatsOffline(
                    newTotal - registered
            );
        }


        // -----------------------------------------------------
        // TIRUNELVELI SEATS
        // -----------------------------------------------------

        if (course.getTotalSeatsTirunelveli() != null) {

            int registered =
                    safeValue(
                            existing
                                    .getRegisteredSeatsTirunelveli()
                    );

            int newTotal =
                    course.getTotalSeatsTirunelveli();

            if (newTotal < registered) {

                throw new IllegalArgumentException(
                        "Tirunelveli total seats cannot be less than already registered seats"
                );
            }

            existing.setTotalSeatsTirunelveli(
                    newTotal
            );

            existing.setAvailableSeatsTirunelveli(
                    newTotal - registered
            );
        }


        // -----------------------------------------------------
        // TISAIYANVILAI SEATS
        // -----------------------------------------------------

        if (course.getTotalSeatsTisaiyanvilai() != null) {

            int registered =
                    safeValue(
                            existing
                                    .getRegisteredSeatsTisaiyanvilai()
                    );

            int newTotal =
                    course.getTotalSeatsTisaiyanvilai();

            if (newTotal < registered) {

                throw new IllegalArgumentException(
                        "Tisaiyanvilai total seats cannot be less than already registered seats"
                );
            }

            existing.setTotalSeatsTisaiyanvilai(
                    newTotal
            );

            existing.setAvailableSeatsTisaiyanvilai(
                    newTotal - registered
            );
        }


        // -----------------------------------------------------
        // FINAL OFFLINE CAPACITY CHECK
        // -----------------------------------------------------

        validateUpdatedOfflineCapacity(
                existing
        );


        // -----------------------------------------------------
        // STATUS
        // -----------------------------------------------------

        if (course.getStatus() != null
                && !course.getStatus().isBlank()) {

            existing.setStatus(
                    course.getStatus()
                            .trim()
                            .toUpperCase()
            );
        }


        // -----------------------------------------------------
        // IMPORTANT
        // -----------------------------------------------------
        /*
         * NO trainerName update here.
         *
         * NO zoomLink update here.
         *
         * These belong to staff/batch assignment logic.
         */


        // -----------------------------------------------------
        // SAVE
        // -----------------------------------------------------

        repository.save(existing);


        // -----------------------------------------------------
        // RESPONSE
        // -----------------------------------------------------

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "courseId",
                id.toString()
        );

        response.put(
                "courseCode",
                existing.getCourseCode()
        );

        response.put(
                "batchId",
                existing.getBatchId()
        );

        response.put(
                "message",
                "Course Updated Successfully"
        );

        return response;
    }


    // =========================================================
    // VALIDATE UPDATED DATES
    // =========================================================

    private void validateUpdatedDates(
            Course course) {

        if (course.getStartDate() == null
                || course.getEndDate() == null) {

            return;
        }

        if (course.getStartDate()
                .isAfter(course.getEndDate())) {

            throw new IllegalArgumentException(
                    "Start Date cannot be after End Date"
            );
        }

        if (course.getRegistrationStartDate() != null
                && course.getRegistrationEndDate() != null) {

            if (course.getRegistrationStartDate()
                    .isAfter(
                            course.getRegistrationEndDate()
                    )) {

                throw new IllegalArgumentException(
                        "Registration Start Date cannot be after Registration End Date"
                );
            }

            if (course.getRegistrationEndDate()
                    .isAfter(
                            course.getStartDate()
                    )) {

                throw new IllegalArgumentException(
                        "Registration End Date cannot be after Course Start Date"
                );
            }
        }
    }


    // =========================================================
    // VALIDATE UPDATED OFFLINE CAPACITY
    // =========================================================

    private void validateUpdatedOfflineCapacity(
            Course course) {

        int offline =
                safeValue(
                        course.getTotalSeatsOffline()
                );

        int tirunelveli =
                safeValue(
                        course.getTotalSeatsTirunelveli()
                );

        int tisaiyanvilai =
                safeValue(
                        course.getTotalSeatsTisaiyanvilai()
                );

        int locationTotal =
                tirunelveli +
                        tisaiyanvilai;

        if (locationTotal > offline) {

            throw new IllegalArgumentException(
                    "Tirunelveli and Tisaiyanvilai seats cannot exceed total offline seats"
            );
        }
    }


    // =========================================================
    // DELETE COURSE
    // =========================================================

    @Transactional
    public void deleteCourse(Long id) {

        Course course =
                repository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Course Not Found"
                                )
                        );

        repository.delete(course);
    }


    // =========================================================
    // UPDATE STATUS
    // =========================================================

    @Transactional
    public Map<String, String> updateStatus(
            Long id,
            String status) {

        Course course =
                repository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Course Not Found with ID: "
                                                + id
                                )
                        );

        if (status == null
                || status.isBlank()) {

            throw new IllegalArgumentException(
                    "Status is required"
            );
        }

        String normalizedStatus =
                status.trim().toUpperCase();


        // -----------------------------------------------------
        // ALLOWED STATUS
        // -----------------------------------------------------

        if (!normalizedStatus.equals("ACTIVE")
                && !normalizedStatus.equals("INACTIVE")
                && !normalizedStatus.equals("CLOSED")) {

            throw new IllegalArgumentException(
                    "Invalid status. Allowed values: ACTIVE, INACTIVE, CLOSED"
            );
        }


        course.setStatus(
                normalizedStatus
        );

        repository.save(course);


        return Map.of(
                "message",
                "Course status updated to " +
                        course.getStatus() +
                        " successfully"
        );
    }
}