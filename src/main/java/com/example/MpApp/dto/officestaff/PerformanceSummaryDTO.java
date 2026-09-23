package com.example.MpApp.dto.officestaff;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PerformanceSummaryDTO {
    private String staffName;
    private int currentScore;
    private long completedTasks;
    private long pendingTasks;
    private double approvalRate; // (Completed / Total Reviewed)
    private double attendancePercentage;
}