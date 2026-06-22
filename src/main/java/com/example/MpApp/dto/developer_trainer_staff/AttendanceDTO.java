package com.example.MpApp.dto.developer_trainer_staff;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AttendanceDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long batchId;
    private String batchName;
    private LocalDate date;
    private boolean present;

    public boolean isPresent(){
        return this.present;
    }

}