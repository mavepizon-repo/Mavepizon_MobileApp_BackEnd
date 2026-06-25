package com.example.MpApp.dto.officestaff;

import com.example.MpApp.dto.officestaff.PerformanceSummaryDTO; // Import your DTO
import lombok.Data;

@Data
public class OfficeStaffProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private int score; // Performance Score
    private String profile;

    // Task Counts
    private long assignedTasks;
    private long completedTasks;
    private long pendingTasks;
    private long rejectedTasks;

    // Performance Analytics
    private PerformanceSummaryDTO performanceMetrics;
}