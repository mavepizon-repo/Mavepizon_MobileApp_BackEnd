package com.example.MpApp.entity.internship;

import com.example.MpApp.entity.student.Student;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "internship_registrations")
@Data
public class InternshipRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false) // Links to the 'id' of the Student table
    private Student student; // Renamed from studentId for clarity

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    private String phone;
    private String gender;
    private LocalDate dob;

    @Column(name = "college_name")
    private String collegeName;

    private String department;
    private String year;
    private String address;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "internship_code_with_name", nullable = false)
    private String internshipCodeWithName;

    @Column(name = "registered_courses_count")
    private Integer registeredCoursesCount = 0;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "batch_code")
    private String batchCode;

    @Column(name = "certificate_status")
    private String certificateStatus; // Default: "NOT_GENERATED"

    @Column(name = "payment_status")
    private String paymentStatus; // Default: "UNPAID"

    @Column(name = "payment_for")
    private String paymentFor; // "Registration", "balance fees", "Full fees"

    @Column(name = "is_online")
    private Boolean isOnline;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.registrationDate = LocalDate.now();
        this.certificateStatus = "NOT_GENERATED";
        this.paymentStatus = "UNPAID";
        this.createdAt = LocalDateTime.now();
    }

}