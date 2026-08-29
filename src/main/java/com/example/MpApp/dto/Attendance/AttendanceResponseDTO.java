package com.example.MpApp.dto.Attendance;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class AttendanceResponseDTO {

    private Long id;

    private LocalDate attendanceDate;

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    private String status;

    // Staff details
    private String staffName;

    private String branch;
}