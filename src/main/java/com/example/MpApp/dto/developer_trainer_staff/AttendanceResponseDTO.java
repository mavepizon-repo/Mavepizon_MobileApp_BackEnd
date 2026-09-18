package com.example.MpApp.dto.developer_trainer_staff;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AttendanceResponseDTO {

    /*
    =========================================
    ATTENDANCE ID
    =========================================
    */

    private Long id;


    /*
    =========================================
    STUDENT DETAILS
    =========================================
    */

    private Long studentId;

    private String studentName;


    /*
    =========================================
    COURSE DETAILS
    =========================================
    */

    private Long courseId;

    private String courseName;


    /*
    =========================================
    ATTENDANCE
    =========================================
    */

    private LocalDate date;

    private boolean present;


    /*
    =========================================
    EXPLICIT GETTER
    =========================================
    */

    public boolean isPresent() {
        return this.present;
    }
}