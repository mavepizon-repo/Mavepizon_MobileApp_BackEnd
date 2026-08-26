package com.example.MpApp.entity.course;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "courses",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "courseCode")
        }
)
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }


    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }


    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }


    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }


    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }


    public LocalDate getRegistrationStartDate() {
        return registrationStartDate;
    }

    public void setRegistrationStartDate(
            LocalDate registrationStartDate) {

        this.registrationStartDate =
                registrationStartDate;
    }


    public LocalDate getRegistrationEndDate() {
        return registrationEndDate;
    }

    public void setRegistrationEndDate(
            LocalDate registrationEndDate) {

        this.registrationEndDate =
                registrationEndDate;
    }


    public Double getTotalFees() {
        return totalFees;
    }

    public void setTotalFees(Double totalFees) {
        this.totalFees = totalFees;
    }


    public Double getRegistrationFees() {
        return registrationFees;
    }

    public void setRegistrationFees(
            Double registrationFees) {

        this.registrationFees =
                registrationFees;
    }


    public Integer getTotalSeatsOnline() {
        return totalSeatsOnline;
    }

    public void setTotalSeatsOnline(
            Integer totalSeatsOnline) {

        this.totalSeatsOnline =
                totalSeatsOnline;
    }


    public Integer getRegisteredSeatsOnline() {
        return registeredSeatsOnline;
    }

    public void setRegisteredSeatsOnline(
            Integer registeredSeatsOnline) {

        this.registeredSeatsOnline =
                registeredSeatsOnline;
    }


    public Integer getAvailableSeatsOnline() {
        return availableSeatsOnline;
    }

    public void setAvailableSeatsOnline(
            Integer availableSeatsOnline) {

        this.availableSeatsOnline =
                availableSeatsOnline;
    }


    public Integer getTotalSeatsOffline() {
        return totalSeatsOffline;
    }

    public void setTotalSeatsOffline(
            Integer totalSeatsOffline) {

        this.totalSeatsOffline =
                totalSeatsOffline;
    }


    public Integer getRegisteredSeatsOffline() {
        return registeredSeatsOffline;
    }

    public void setRegisteredSeatsOffline(
            Integer registeredSeatsOffline) {

        this.registeredSeatsOffline =
                registeredSeatsOffline;
    }


    public Integer getAvailableSeatsOffline() {
        return availableSeatsOffline;
    }

    public void setAvailableSeatsOffline(
            Integer availableSeatsOffline) {

        this.availableSeatsOffline =
                availableSeatsOffline;
    }


    public Integer getTotalSeatsTirunelveli() {
        return totalSeatsTirunelveli;
    }

    public void setTotalSeatsTirunelveli(
            Integer totalSeatsTirunelveli) {

        this.totalSeatsTirunelveli =
                totalSeatsTirunelveli;
    }


    public Integer getRegisteredSeatsTirunelveli() {
        return registeredSeatsTirunelveli;
    }

    public void setRegisteredSeatsTirunelveli(
            Integer registeredSeatsTirunelveli) {

        this.registeredSeatsTirunelveli =
                registeredSeatsTirunelveli;
    }


    public Integer getAvailableSeatsTirunelveli() {
        return availableSeatsTirunelveli;
    }

    public void setAvailableSeatsTirunelveli(
            Integer availableSeatsTirunelveli) {

        this.availableSeatsTirunelveli =
                availableSeatsTirunelveli;
    }


    public Integer getTotalSeatsTisaiyanvilai() {
        return totalSeatsTisaiyanvilai;
    }

    public void setTotalSeatsTisaiyanvilai(
            Integer totalSeatsTisaiyanvilai) {

        this.totalSeatsTisaiyanvilai =
                totalSeatsTisaiyanvilai;
    }


    public Integer getRegisteredSeatsTisaiyanvilai() {
        return registeredSeatsTisaiyanvilai;
    }

    public void setRegisteredSeatsTisaiyanvilai(
            Integer registeredSeatsTisaiyanvilai) {

        this.registeredSeatsTisaiyanvilai =
                registeredSeatsTisaiyanvilai;
    }


    public Integer getAvailableSeatsTisaiyanvilai() {
        return availableSeatsTisaiyanvilai;
    }

    public void setAvailableSeatsTisaiyanvilai(
            Integer availableSeatsTisaiyanvilai) {

        this.availableSeatsTisaiyanvilai =
                availableSeatsTisaiyanvilai;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getZoomLink() {
        return zoomLink;
    }

    public void setZoomLink(String zoomLink) {
        this.zoomLink = zoomLink;
    }


    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt) {

        this.updatedAt = updatedAt;
    }
}