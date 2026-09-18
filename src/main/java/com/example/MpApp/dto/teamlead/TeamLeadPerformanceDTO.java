package com.example.MpApp.dto.teamlead;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamLeadPerformanceDTO {
    private Long teamLeadId;
    private String name;
    private Integer performanceScore;
    private Long assignedTasks;
    private Long completedTasks;
    private Long pendingTasks;
}