package com.example.MpApp.dto.officestaff;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveRequestDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;

    // Optional: Add a field to help distinguish between full day, half day, etc.
    private String leaveType;
}