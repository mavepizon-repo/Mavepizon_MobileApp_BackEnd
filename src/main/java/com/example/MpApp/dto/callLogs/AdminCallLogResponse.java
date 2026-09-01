package com.example.MpApp.dto.callLogs;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCallLogResponse {
    private String callId;
    private String staffName;
    private String staffBranch;
    private String enquiryId;
    private String phoneNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationSeconds;
    // getters/setters
}