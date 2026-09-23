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
import com.example.MpApp.exception.OwnershipViolationException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TelecallingCallLogService {

    private final TelecallingCallLogRepository repo;
    private final OfficeStaffRepository staffRepo;
    private final TelecallingEnquiryRepository enquiryRepo;
    private final JwtService jwtService;

    public TelecallingCallLogService(
            TelecallingCallLogRepository repo,
            OfficeStaffRepository staffRepo,
            TelecallingEnquiryRepository enquiryRepo,
            JwtService jwtService) {

        this.repo = repo;
        this.staffRepo = staffRepo;
        this.enquiryRepo = enquiryRepo;
        this.jwtService = jwtService;
    }

    public String extractEmail(String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token Required");
        }

        String token = authHeader.substring(7);
        return jwtService.extractUsername(token);
    }

    @Transactional
    public CallLogResponse saveCall(
            String authHeader,
            CallLogRequest req) {

        String email = extractEmail(authHeader);

        OfficeStaff staff = staffRepo.findByEmail(email)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Staff not found: " + email));

        Long enquiryId = Long.parseLong(req.getEnquiryId());

        TelecallingEnquiry enquiry = enquiryRepo.findById(enquiryId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Enquiry not found: " + enquiryId));

        if (enquiry.getStaff() == null ||
                !enquiry.getStaff().getId().equals(staff.getId())) {
            throw new OwnershipViolationException(
                    "You do not own this enquiry");
        }

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

    public List<CallLogResponse> getCallsForEnquiry(
            String authHeader, Long enquiryId) {

        OfficeStaff staff = getAuthenticatedStaff(authHeader);
        verifyEnquiryOwnership(enquiryId, staff.getId());

        return repo.findByEnquiryIdAndStaffIdOrderByCallTimeDesc(
                        enquiryId, staff.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CallLogResponse updateStatus(
            String authHeader, Long callId, String status) {

        OfficeStaff staff = getAuthenticatedStaff(authHeader);

        TelecallingCallLog call = repo.findById(callId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Call not found: " + callId));

        if (call.getStaff() == null ||
                !call.getStaff().getId().equals(staff.getId())) {
            throw new OwnershipViolationException(
                    "You do not own this call log");
        }

        call.setCallStatus(status);

        return toResponse(repo.save(call));
    }

    private OfficeStaff getAuthenticatedStaff(String authHeader) {
        String email = extractEmail(authHeader);
        return staffRepo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Staff not found: " + email));
    }

    private void verifyEnquiryOwnership(Long enquiryId, Long staffId) {
        TelecallingEnquiry enquiry = enquiryRepo.findById(enquiryId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Enquiry not found: " + enquiryId));

        if (enquiry.getStaff() == null ||
                !enquiry.getStaff().getId().equals(staffId)) {
            throw new OwnershipViolationException(
                    "You do not own this enquiry");
        }
    }

    public List<CallLogResponse> getCallsByDate() {

        List<TelecallingCallLog> logs =
                repo.findByCreatedDate(LocalDate.now());

        return logs.stream()
                .map(this::toResponse)
                .toList();
    }

    public List<CallLogResponse> getCallsByStatus(
            String status) {

        List<TelecallingCallLog> logs =
                repo.findByCallStatus(status);

        return logs.stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<AdminCallLogResponse> getAdminCalls(
            LocalDate date,
            Long staffId,
            Pageable pageable) {

        return repo.findAll(pageable)
                .map(c -> new AdminCallLogResponse(
                        c.getId(),
                        "CALL" + c.getId(),
                        c.getStaff().getName(),
                        c.getStaff().getBranch(),
                        String.valueOf(c.getEnquiry().getId()),
                        c.getEnquiry().getPhone(),
                        c.getCallStatus(),
                        c.getCallTime(),
                        c.getEndTime(),
                        c.getDurationSeconds()
                ));
    }

    private CallLogResponse toResponse(
            TelecallingCallLog c) {

        CallLogResponse r = new CallLogResponse();

        r.setCallId("CALL" + c.getId());
        r.setEnquiryId(
                String.valueOf(c.getEnquiry().getId()));
        r.setStaffName(
                c.getStaff().getName());
        r.setStaffBranch(
                c.getStaff().getBranch());
        r.setPhoneNumber(
                c.getEnquiry().getPhone());
        r.setCallStatus(
                CallStatus.valueOf(c.getCallStatus()));
        r.setStartTime(
                c.getCallTime());
        r.setAnsweredTime(
                c.getAnsweredTime());
        r.setEndTime(
                c.getEndTime());
        r.setDurationSeconds(
                c.getDurationSeconds());
        r.setId(c.getId());

        return r;
    }
}