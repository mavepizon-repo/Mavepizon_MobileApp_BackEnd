package com.example.MpApp.service.course;

import com.example.MpApp.entity.course.Category;
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
public class InternshipService {

    @Autowired
    private CourseRepository repository;


    // =========================================================
    // GENERATE INTERNSHIP CODE
    // =========================================================
    /*
     * Format:
     *
     * MPIN1001
     * MPIN1002
     * MPIN1003
     */
    private String generateInternshipCode() {

        Optional<Course> lastInternship =
                repository.findTopByCourseCodeStartingWithOrderByCourseCodeDesc("MPIN");

        long nextNumber = 1001;

        if (lastInternship.isPresent()
                && lastInternship.get().getCourseCode() != null) {

            String lastCode =
                    lastInternship.get().getCourseCode();

            try {

                String numberPart =
                        lastCode.substring(4);

                nextNumber =
                        Long.parseLong(numberPart) + 1;

            } catch (NumberFormatException e) {

                throw new RuntimeException(
                        "Invalid existing internship code format: " + lastCode
                );
            }
        }

        return "MPIN" + nextNumber;
    }


    // =========================================================
    // GENERATE BATCH ID
    // =========================================================
    /*
     * Format:
     *
     * MAV-INTRN-2026-01
     *
     * Derived the same way as Course: from the
     * internship name (courseName field, reused).
     */
    private String generateBatchId(Course internship) {

        if (internship.getCourseName() == null
                || internship.getCourseName().isBlank()) {

            throw new IllegalArgumentException(
                    "Internship Name is required"
            );
        }

        String namePart =
                internship.getCourseName()
                        .trim()
                        .toUpperCase()
                        .replaceAll("[^A-Z0-9]", "");

        if (namePart.length() > 6) {

            namePart =
                    namePart.substring(0, 6);
        }

        int year =
                internship.getStartDate() != null
                        ? internship.getStartDate().getYear()
                        : LocalDate.now().getYear();

        String prefix =
                "MAV-" +
                        namePart +
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
    // CREATE INTERNSHIP
    // =========================================================

    @Transactional
    public Map<String, String> createInternship(
            Course internship) {

        // -----------------------------------------------------
        // BASIC VALIDATION
        // -----------------------------------------------------

        if (internship == null) {

            throw new IllegalArgumentException(
                    "Internship data is required"
            );
        }


        // -----------------------------------------------------
        // INTERNSHIP NAME
        // -----------------------------------------------------

        if (internship.getCourseName() == null
                || internship.getCourseName().isBlank()) {

            throw new IllegalArgumentException(
                    "Internship Name is required"
            );
        }


        // -----------------------------------------------------
        // DATES
        // -----------------------------------------------------

        if (internship.getStartDate() == null) {

            throw new IllegalArgumentException(
                    "Start Date is required"
            );
        }

        if (internship.getEndDate() == null) {

            throw new IllegalArgumentException(
                    "End Date is required"
            );
        }

        if (internship.getStartDate()
                .isAfter(internship.getEndDate())) {

            throw new IllegalArgumentException(
                    "Start Date cannot be after End Date"
            );
        }


        // -----------------------------------------------------
        // REGISTRATION DATES
        // -----------------------------------------------------

        if (internship.getRegistrationStartDate() == null) {

            throw new IllegalArgumentException(
                    "Registration Start Date is required"
            );
        }

        if (internship.getRegistrationEndDate() == null) {

            throw new IllegalArgumentException(
                    "Registration End Date is required"
            );
        }

        if (internship.getRegistrationStartDate()
                .isAfter(
                        internship.getRegistrationEndDate()
                )) {

            throw new IllegalArgumentException(
                    "Registration Start Date cannot be after Registration End Date"
            );
        }

        if (internship.getRegistrationEndDate()
                .isAfter(internship.getStartDate())) {

            throw new IllegalArgumentException(
                    "Registration End Date cannot be after Internship Start Date"
            );
        }


        // -----------------------------------------------------
        // FEES
        // -----------------------------------------------------

        if (internship.getTotalFees() == null
                || internship.getTotalFees() < 0) {

            throw new IllegalArgumentException(
                    "Valid Total Fee is required"
            );
        }

        double registrationFee =
                internship.getTotalFees() * 0.10;

        internship.setRegistrationFees(
                registrationFee
        );


        // -----------------------------------------------------
        // INTERNSHIP CODE
        // -----------------------------------------------------

        internship.setCourseCode(
                generateInternshipCode()
        );


        // -----------------------------------------------------
        // BATCH ID
        // -----------------------------------------------------

        internship.setBatchId(
                generateBatchId(internship)
        );


        // -----------------------------------------------------
        // SEAT VALIDATION
        // -----------------------------------------------------

        validateSeats(internship);


        // -----------------------------------------------------
        // INITIAL REGISTERED COUNTS
        // -----------------------------------------------------

        internship.setRegisteredSeatsOnline(0);

        internship.setRegisteredSeatsOffline(0);

        internship.setRegisteredSeatsTirunelveli(0);

        internship.setRegisteredSeatsTisaiyanvilai(0);


        // -----------------------------------------------------
        // INITIAL AVAILABLE COUNTS
        // -----------------------------------------------------

        internship.setAvailableSeatsOnline(
                safeValue(
                        internship.getTotalSeatsOnline()
                )
        );

        internship.setAvailableSeatsOffline(
                safeValue(
                        internship.getTotalSeatsOffline()
                )
        );

        internship.setAvailableSeatsTirunelveli(
                safeValue(
                        internship.getTotalSeatsTirunelveli()
                )
        );

        internship.setAvailableSeatsTisaiyanvilai(
                safeValue(
                        internship.getTotalSeatsTisaiyanvilai()
                )
        );


        // -----------------------------------------------------
        // STATUS
        // -----------------------------------------------------

        if (internship.getStatus() == null
                || internship.getStatus().isBlank()) {

            internship.setStatus("ACTIVE");

        } else {

            internship.setStatus(
                    internship.getStatus()
                            .trim()
                            .toUpperCase()
            );
        }

        internship.setCategory(Category.INTERNSHIP);


        // -----------------------------------------------------
        // SAVE
        // -----------------------------------------------------

        Course savedInternship =
                repository.save(internship);


        // -----------------------------------------------------
        // RESPONSE
        // -----------------------------------------------------

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "internshipId",
                savedInternship.getId().toString()
        );

        response.put(
                "internshipCode",
                savedInternship.getCourseCode()
        );

        response.put(
                "batchId",
                savedInternship.getBatchId()
        );

        response.put(
                "message",
                "Internship Created Successfully"
        );

        return response;
    }


    // =========================================================
    // VALIDATE SEATS
    // =========================================================

    private void validateSeats(Course internship) {

        Integer online =
                internship.getTotalSeatsOnline();

        Integer offline =
                internship.getTotalSeatsOffline();

        Integer tirunelveli =
                internship.getTotalSeatsTirunelveli();

        Integer tisaiyanvilai =
                internship.getTotalSeatsTisaiyanvilai();


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

        internship.setTotalSeatsOnline(online);

        internship.setTotalSeatsOffline(offline);

        internship.setTotalSeatsTirunelveli(
                tirunelveli
        );

        internship.setTotalSeatsTisaiyanvilai(
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
    // GET ALL INTERNSHIPS
    // =========================================================

    public List<Course> getAllInternships() {

        return repository.findByCategory(
                Category.INTERNSHIP
        );
    }


    // =========================================================
    // GET BY DATABASE ID
    // =========================================================

    public Course getInternshipById(Long id) {

        return repository.findByIdAndCategory(
                        id,
                        Category.INTERNSHIP
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Internship Not Found"
                        )
                );
    }


    // =========================================================
    // GET BY BATCH ID
    // =========================================================

    public Optional<Course> getInternshipByBatchId(
            String batchId) {

        if (batchId == null
                || batchId.isBlank()) {

            throw new IllegalArgumentException(
                    "Batch ID is required"
            );
        }

        return repository.findByBatchIdAndCategory(
                batchId.trim(),
                Category.INTERNSHIP
        );
    }


    // =========================================================
    // GET BY INTERNSHIP CODE
    // =========================================================

    public Optional<Course> getInternshipByCode(
            String internshipCode) {

        if (internshipCode == null
                || internshipCode.isBlank()) {

            throw new IllegalArgumentException(
                    "Internship Code is required"
            );
        }

        return repository.findByCourseCodeAndCategory(
                internshipCode.trim(),
                Category.INTERNSHIP
        );
    }


    // =========================================================
    // UPDATE INTERNSHIP
    // =========================================================
    /*
     * Internship code cannot be changed.
     * Batch ID cannot be changed.
     *
     * Trainer and Zoom Link are NOT handled here.
     */

    @Transactional
    public Map<String, String> updateInternship(
            Long id,
            Course internship) {

        // -----------------------------------------------------
        // FIND EXISTING INTERNSHIP (scoped to category)
        // -----------------------------------------------------

        Course existing =
                repository.findByIdAndCategory(
                                id,
                                Category.INTERNSHIP
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Internship Not Found"
                                )
                        );


        // -----------------------------------------------------
        // NAME
        // -----------------------------------------------------

        if (internship.getCourseName() != null
                && !internship.getCourseName().isBlank()) {

            existing.setCourseName(
                    internship.getCourseName()
            );
        }


        // -----------------------------------------------------
        // DESCRIPTION
        // -----------------------------------------------------

        if (internship.getDescription() != null) {

            existing.setDescription(
                    internship.getDescription()
            );
        }


        // -----------------------------------------------------
        // DURATION
        // -----------------------------------------------------

        if (internship.getDuration() != null) {

            existing.setDuration(
                    internship.getDuration()
            );
        }


        // -----------------------------------------------------
        // DATES
        // -----------------------------------------------------

        if (internship.getStartDate() != null) {

            existing.setStartDate(
                    internship.getStartDate()
            );
        }

        if (internship.getEndDate() != null) {

            existing.setEndDate(
                    internship.getEndDate()
            );
        }


        // -----------------------------------------------------
        // REGISTRATION DATES
        // -----------------------------------------------------

        if (internship.getRegistrationStartDate() != null) {

            existing.setRegistrationStartDate(
                    internship.getRegistrationStartDate()
            );
        }

        if (internship.getRegistrationEndDate() != null) {

            existing.setRegistrationEndDate(
                    internship.getRegistrationEndDate()
            );
        }


        // -----------------------------------------------------
        // VALIDATE DATES
        // -----------------------------------------------------

        validateUpdatedDates(existing);


        // -----------------------------------------------------
        // FEES
        // -----------------------------------------------------

        if (internship.getTotalFees() != null) {

            if (internship.getTotalFees() < 0) {

                throw new IllegalArgumentException(
                        "Total Fee cannot be negative"
                );
            }

            existing.setTotalFees(
                    internship.getTotalFees()
            );

            existing.setRegistrationFees(
                    internship.getTotalFees() * 0.30
            );
        }


        // -----------------------------------------------------
        // ONLINE SEATS
        // -----------------------------------------------------

        if (internship.getTotalSeatsOnline() != null) {

            int registered =
                    safeValue(
                            existing
                                    .getRegisteredSeatsOnline()
                    );

            int newTotal =
                    internship.getTotalSeatsOnline();

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

        if (internship.getTotalSeatsOffline() != null) {

            int registered =
                    safeValue(
                            existing
                                    .getRegisteredSeatsOffline()
                    );

            int newTotal =
                    internship.getTotalSeatsOffline();

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

        if (internship.getTotalSeatsTirunelveli() != null) {

            int registered =
                    safeValue(
                            existing
                                    .getRegisteredSeatsTirunelveli()
                    );

            int newTotal =
                    internship.getTotalSeatsTirunelveli();

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

        if (internship.getTotalSeatsTisaiyanvilai() != null) {

            int registered =
                    safeValue(
                            existing
                                    .getRegisteredSeatsTisaiyanvilai()
                    );

            int newTotal =
                    internship.getTotalSeatsTisaiyanvilai();

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

        if (internship.getStatus() != null
                && !internship.getStatus().isBlank()) {

            existing.setStatus(
                    internship.getStatus()
                            .trim()
                            .toUpperCase()
            );
        }


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
                "internshipId",
                id.toString()
        );

        response.put(
                "internshipCode",
                existing.getCourseCode()
        );

        response.put(
                "batchId",
                existing.getBatchId()
        );

        response.put(
                "message",
                "Internship Updated Successfully"
        );

        return response;
    }


    // =========================================================
    // VALIDATE UPDATED DATES
    // =========================================================

    private void validateUpdatedDates(
            Course internship) {

        if (internship.getStartDate() == null
                || internship.getEndDate() == null) {

            return;
        }

        if (internship.getStartDate()
                .isAfter(internship.getEndDate())) {

            throw new IllegalArgumentException(
                    "Start Date cannot be after End Date"
            );
        }

        if (internship.getRegistrationStartDate() != null
                && internship.getRegistrationEndDate() != null) {

            if (internship.getRegistrationStartDate()
                    .isAfter(
                            internship.getRegistrationEndDate()
                    )) {

                throw new IllegalArgumentException(
                        "Registration Start Date cannot be after Registration End Date"
                );
            }

            if (internship.getRegistrationEndDate()
                    .isAfter(
                            internship.getStartDate()
                    )) {

                throw new IllegalArgumentException(
                        "Registration End Date cannot be after Internship Start Date"
                );
            }
        }
    }


    // =========================================================
    // VALIDATE UPDATED OFFLINE CAPACITY
    // =========================================================

    private void validateUpdatedOfflineCapacity(
            Course internship) {

        int offline =
                safeValue(
                        internship.getTotalSeatsOffline()
                );

        int tirunelveli =
                safeValue(
                        internship.getTotalSeatsTirunelveli()
                );

        int tisaiyanvilai =
                safeValue(
                        internship.getTotalSeatsTisaiyanvilai()
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
    // DELETE INTERNSHIP
    // =========================================================

    @Transactional
    public void deleteInternship(Long id) {

        Course internship =
                repository.findByIdAndCategory(
                                id,
                                Category.INTERNSHIP
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Internship Not Found"
                                )
                        );

        repository.delete(internship);
    }


    // =========================================================
    // UPDATE STATUS
    // =========================================================

    @Transactional
    public Map<String, String> updateStatus(
            Long id,
            String status) {

        Course internship =
                repository.findByIdAndCategory(
                                id,
                                Category.INTERNSHIP
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Internship Not Found with ID: "
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


        internship.setStatus(
                normalizedStatus
        );

        repository.save(internship);


        return Map.of(
                "message",
                "Internship status updated to " +
                        internship.getStatus() +
                        " successfully"
        );
    }
}