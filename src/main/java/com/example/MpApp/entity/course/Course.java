package com.example.MpApp.entity.course;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;




@Entity
@Table(
        name = "courses",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "courseCode")
        }
)
@Data
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================================================
    // COURSE INFORMATION
    // =========================================================

    @Column(nullable = false, unique = true)
    private String courseCode;

    @Column(name = "batch_id", nullable = false, unique = true)
    private String batchId;

    @Column(nullable = false)
    private String courseName;

    @Column(length = 2000)
    private String description;

    private String duration;

    private Category category;


    // =========================================================
    // COURSE DATES
    // =========================================================

    private LocalDate startDate;

    private LocalDate endDate;


    // =========================================================
    // REGISTRATION PERIOD
    // =========================================================

    private LocalDate registrationStartDate;

    private LocalDate registrationEndDate;


    // =========================================================
    // FEES
    // =========================================================

    private Double totalFees;

    private Double registrationFees;


    // =========================================================
    // ONLINE SEATS
    // =========================================================

    private Integer totalSeatsOnline;

    private Integer registeredSeatsOnline;

    private Integer availableSeatsOnline;


    // =========================================================
    // OFFLINE SEATS
    // =========================================================

    private Integer totalSeatsOffline;

    private Integer registeredSeatsOffline;

    private Integer availableSeatsOffline;


    // =========================================================
    // TIRUNELVELI SEATS
    // =========================================================

    private Integer totalSeatsTirunelveli;

    private Integer registeredSeatsTirunelveli;

    private Integer availableSeatsTirunelveli;


    // =========================================================
    // TISAIYANVILAI SEATS
    // =========================================================

    private Integer totalSeatsTisaiyanvilai;

    private Integer registeredSeatsTisaiyanvilai;

    private Integer availableSeatsTisaiyanvilai;


    // =========================================================
    // OTHER INFORMATION
    // =========================================================

    private String status;

    private String zoomLink;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Course() {
    }


    // =========================================================
    // PRE PERSIST
    // =========================================================

    @PrePersist
    public void prePersist() {

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // -----------------------------------------------------
        // DEFAULT STATUS
        // -----------------------------------------------------

        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }


        // -----------------------------------------------------
        // ONLINE SEATS
        // -----------------------------------------------------

        if (registeredSeatsOnline == null) {
            registeredSeatsOnline = 0;
        }

        if (totalSeatsOnline != null) {

            if (registeredSeatsOnline < 0) {
                registeredSeatsOnline = 0;
            }

            if (registeredSeatsOnline > totalSeatsOnline) {
                throw new IllegalArgumentException(
                        "Registered online seats cannot exceed total online seats"
                );
            }

            availableSeatsOnline =
                    totalSeatsOnline - registeredSeatsOnline;
        }


        // -----------------------------------------------------
        // OFFLINE SEATS
        // -----------------------------------------------------

        if (registeredSeatsOffline == null) {
            registeredSeatsOffline = 0;
        }

        if (totalSeatsOffline != null) {

            if (registeredSeatsOffline < 0) {
                registeredSeatsOffline = 0;
            }

            if (registeredSeatsOffline > totalSeatsOffline) {
                throw new IllegalArgumentException(
                        "Registered offline seats cannot exceed total offline seats"
                );
            }

            availableSeatsOffline =
                    totalSeatsOffline - registeredSeatsOffline;
        }


        // -----------------------------------------------------
        // TIRUNELVELI SEATS
        // -----------------------------------------------------

        if (registeredSeatsTirunelveli == null) {
            registeredSeatsTirunelveli = 0;
        }

        if (totalSeatsTirunelveli != null) {

            if (registeredSeatsTirunelveli < 0) {
                registeredSeatsTirunelveli = 0;
            }

            if (registeredSeatsTirunelveli >
                    totalSeatsTirunelveli) {

                throw new IllegalArgumentException(
                        "Registered Tirunelveli seats cannot exceed total Tirunelveli seats"
                );
            }

            availableSeatsTirunelveli =
                    totalSeatsTirunelveli
                            - registeredSeatsTirunelveli;
        }


        // -----------------------------------------------------
        // TISAIYANVILAI SEATS
        // -----------------------------------------------------

        if (registeredSeatsTisaiyanvilai == null) {
            registeredSeatsTisaiyanvilai = 0;
        }

        if (totalSeatsTisaiyanvilai != null) {

            if (registeredSeatsTisaiyanvilai >
                    totalSeatsTisaiyanvilai) {

                throw new IllegalArgumentException(
                        "Registered Tisaiyanvilai seats cannot exceed total Tisaiyanvilai seats"
                );
            }

            availableSeatsTisaiyanvilai =
                    totalSeatsTisaiyanvilai
                            - registeredSeatsTisaiyanvilai;
        }
    }


    // =========================================================
    // PRE UPDATE
    // =========================================================

    @PreUpdate
    public void preUpdate() {

        /*
         * Do NOT reset available seats here.
         *
         * Example:
         *
         * Total     = 50
         * Registered = 10
         * Available = 40
         *
         * Updating the course must not reset
         * available seats back to 50.
         */

        updatedAt = LocalDateTime.now();
    }


    // =========================================================
    // GETTERS AND SETTERS
    // =========================================================

}