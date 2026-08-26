package com.example.MpApp.dto.developer_trainer_staff;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AttendanceRequest {

    /*
    =========================================
    COURSE
    =========================================
    */

    private Long courseId;


    /*
    =========================================
    ATTENDANCE DATE
    =========================================
    */

    /*
     * If date is null,
     * backend can use today's date.
     */

    private LocalDate date;


    /*
    =========================================
    STUDENT ATTENDANCE
    =========================================
    */

    /*
     * Multiple students can be marked
     * in a single attendance request.
     */

    private List<StudentAttendanceDto> students;


    /*
    =========================================
    STUDENT ATTENDANCE DTO
    =========================================
    */

    @Data
    public static class StudentAttendanceDto {

        /*
         * Existing Student ID
         */

        private Long studentId;


        /*
         * true  = PRESENT
         * false = ABSENT
         */

        private boolean present;
    }
}