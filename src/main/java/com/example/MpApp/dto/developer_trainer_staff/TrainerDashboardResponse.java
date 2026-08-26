package com.example.MpApp.dto.developer_trainer_staff;

import lombok.Data;

@Data
public class TrainerDashboardResponse {

    /*
    =========================================
    ASSIGNED COURSES
    =========================================
    */

    private Long assignedCourses;


    /*
    =========================================
    TOTAL STUDENTS
    =========================================
    */

    private Long totalStudents;


    /*
    =========================================
    TODAY'S PRESENT STUDENTS
    =========================================
    */

    private Long todayPresent;


    /*
    =========================================
    COURSE MATERIALS
    =========================================
    */

    private Long totalMaterials;


    /*
    =========================================
    ATTENDANCE
    =========================================
    */

    private Long totalAttendanceRecords;
}