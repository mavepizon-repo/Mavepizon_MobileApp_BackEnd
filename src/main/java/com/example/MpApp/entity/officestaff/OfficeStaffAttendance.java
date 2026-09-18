package com.example.MpApp.entity.officestaff;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "office_staff_attendance", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"staff_id", "attendanceDate"})
})
public class OfficeStaffAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private OfficeStaff staff;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    private LocalTime checkInTime;
    private LocalTime checkOutTime;

    @Column(nullable = false)
    private String status; // PRESENT, ABSENT, LATE, OD

    // Constructors
    public OfficeStaffAttendance() {}

    public OfficeStaffAttendance(OfficeStaff staff, LocalDate attendanceDate, String status) {
        this.staff = staff;
        this.attendanceDate = attendanceDate;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public OfficeStaff getStaff() { return staff; }
    public void setStaff(OfficeStaff staff) { this.staff = staff; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public LocalTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}