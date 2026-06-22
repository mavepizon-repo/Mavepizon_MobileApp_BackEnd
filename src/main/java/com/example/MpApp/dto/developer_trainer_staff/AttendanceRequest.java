package com.example.MpApp.dto.developer_trainer_staff;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class AttendanceRequest {
    private Long batchId;
    private LocalDate date; // Optional: defaults to today if null
    private List<StudentAttendanceDto> students;

    @Data
    public static class StudentAttendanceDto {
        private Long studentId;
        private boolean present;
    }
}