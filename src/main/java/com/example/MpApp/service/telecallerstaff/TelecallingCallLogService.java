package com.example.MpApp.service.telecallerstaff;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.dto.callLogs.AdminCallLogResponse;
import com.example.MpApp.dto.callLogs.CallLogRequest;
import com.example.MpApp.dto.callLogs.CallLogResponse;
import com.example.MpApp.dto.callLogs.CallStatus;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.telecallerstaff.TelecallingCallLog;
import com.example.MpApp.entity.telecallerstaff.TelecallingEnquiry;
import com.example.MpApp.repository.officestaff.OfficeStaffRepository;
import com.example.MpApp.repository.telecallerstaff.TelecallingCallLogRepository;
import com.example.MpApp.repository.telecallerstaff.TelecallingEnquiryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TelecallingCallLogService {

    private final TelecallingCallLogRepository repo;
    private final OfficeStaffRepository staffRepo;
    private final TelecallingEnquiryRepository enquiryRepo;

    private final JwtService jwtService;

    public TelecallingCallLogService(TelecallingCallLogRepository repo, OfficeStaffRepository staffRepo, TelecallingEnquiryRepository enquiryRepo, JwtService jwtService) {
        this.repo = repo;
        this.staffRepo = staffRepo;
        this.enquiryRepo = enquiryRepo;
        this.jwtService = jwtService;
    }


    public String extractEmail(String authHeader){
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new RuntimeException("Token Required");
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);

        return email;
    }


    @Transactional
    public CallLogResponse saveCall(String authHeader, CallLogRequest req) {

        String email = extractEmail(authHeader);
        OfficeStaff staff = staffRepo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Staff not found: " + email));

        Long enquiryId = Long.parseLong(req.getEnquiryId()); // will throw if null/blank — enquiry is required
        TelecallingEnquiry enquiry = enquiryRepo.findById(enquiryId)
                .orElseThrow(() -> new EntityNotFoundException("Enquiry not found: " + enquiryId));

        TelecallingCallLog call = new TelecallingCallLog();
        call.setStaff(staff);
        call.setEnquiry(enquiry);
        call.setCallStatus(String.valueOf(req.getCallStatus()));
        call.setCallTime(req.getStartTime());
        call.setAnsweredTime(req.getAnsweredTime());
        call.setEndTime(req.getEndTime());
        call.setDurationSeconds(req.getDurationSeconds());

        TelecallingCallLog saved = repo.save(call);
        return toResponse(saved);
    }

    public List<CallLogResponse> getCallsForEnquiry(Long enquiryId) {
        return repo.findByEnquiryIdOrderByCallTimeDesc(enquiryId)
                .stream().map(this::toResponse).toList();
    }

    public List<CallLogResponse> getCallsByDate() {
        List<TelecallingCallLog> logs = repo.findByCreatedDate(LocalDate.now());

        return logs.stream()
                .map(this::toResponse)
                .toList();
    }

    public List<CallLogResponse> getCallsByStatus(String status) {
        List<TelecallingCallLog> logs = repo.findByCallStatus(status);
        return logs.stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AdminCallLogResponse> getAdminCalls(LocalDate date, Long staffId) {
        return repo.findAll().stream()
                .map(c -> new AdminCallLogResponse(
                        "CALL" + c.getId(),
                        c.getStaff().getName(),
                        c.getStaff().getBranch(),        // adjust if branch is a relation, e.g. c.getStaff().getBranch().getName()
                        String.valueOf(c.getEnquiry().getId()),
                        c.getEnquiry().getPhone(),
                        c.getCallTime(),
                        c.getEndTime(),
                        c.getDurationSeconds()
                )).toList();
    }

    private CallLogResponse toResponse(TelecallingCallLog c) {
        CallLogResponse r = new CallLogResponse();
        r.setCallId("CALL" + c.getId());
        r.setEnquiryId(String.valueOf(c.getEnquiry().getId()));
        r.setStaffName(c.getStaff().getName());
        r.setStaffBranch(c.getStaff().getBranch());     // same adjustment as above if it's a relation
        r.setPhoneNumber(c.getEnquiry().getPhone());
        r.setCallStatus(CallStatus.valueOf(c.getCallStatus()));
        r.setStartTime(c.getCallTime());
        r.setAnsweredTime(c.getAnsweredTime());
        r.setEndTime(c.getEndTime());
        r.setDurationSeconds(c.getDurationSeconds());
        return r;
    }
}
