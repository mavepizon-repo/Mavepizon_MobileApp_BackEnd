package com.example.MpApp.dto.callLogs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallLogResponse {
    private Long id;
    private String callId;
    private String enquiryId;
    private String staffName;
    private String staffBranch;
    private String phoneNumber;
    private CallStatus callStatus;
    private LocalDateTime startTime;
    private LocalDateTime answeredTime;
    private LocalDateTime endTime;
    private Long durationSeconds;
    // getters/setters
}
