package com.example.MpApp.dto.callLogs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallLogRequest {
    private String enquiryId;   // "123" -> parsed to Long in service
    private String staffId;     // present in body but path param wins — see controller
    private String phoneNumber; // ignored on save now, since it lives on Enquiry; kept for backward compat / validation
    private CallStatus callStatus;
    private LocalDateTime startTime;
    private LocalDateTime answeredTime;
    private LocalDateTime endTime;
    private Long durationSeconds;
    // getters/setters
}