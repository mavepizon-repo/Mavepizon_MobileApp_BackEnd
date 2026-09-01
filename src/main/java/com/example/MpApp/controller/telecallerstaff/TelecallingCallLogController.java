package com.example.MpApp.controller.telecallerstaff;

import com.example.MpApp.dto.callLogs.CallLogRequest;
import com.example.MpApp.dto.callLogs.CallLogResponse;
import com.example.MpApp.service.telecallerstaff.TelecallingCallLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
public class TelecallingCallLogController {

    private final TelecallingCallLogService service;

    public TelecallingCallLogController(TelecallingCallLogService service) {
        this.service = service;
    }


    @PostMapping("/api/officestaff/telecalling/call")
    public ResponseEntity<?> saveCall(@RequestHeader("Authorization") String authHeader, @RequestBody CallLogRequest req) {
        CallLogResponse saved = service.saveCall(authHeader, req);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of("callId", saved.getCallId())
        ));
    }


    @GetMapping("/api/officestaff/telecalling/enquiry/calls/{enquiryId}")
    public ResponseEntity<?> getCallsForEnquiry(@RequestHeader("Authorization") String authHeader, @PathVariable Long enquiryId) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", service.getCallsForEnquiry(enquiryId)
        ));
    }


    @GetMapping("/api/admin/telecalling/calls")
    public ResponseEntity<?> getAdminCalls(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long staffId) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", service.getAdminCalls(date, staffId)
        ));
    }
}