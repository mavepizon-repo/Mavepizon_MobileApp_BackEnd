package com.example.MpApp.service.telecallerstaff;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.dto.telecallerstaff.TelecallingEnquiryRequest;
import com.example.MpApp.dto.telecallerstaff.TelecallingFollowupRequest;
import com.example.MpApp.dto.telecallerstaff.TelecallingUpdateRequest;
import com.example.MpApp.entity.enums.EnquiryStatus;
import com.example.MpApp.entity.enums.StaffCategory;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.entity.telecallerstaff.TelecallingEnquiry;
import com.example.MpApp.entity.telecallerstaff.TelecallingFollowup;
import com.example.MpApp.repository.officestaff.OfficeStaffRepository;
import com.example.MpApp.repository.student.StudentRepository;
import com.example.MpApp.repository.telecallerstaff.TelecallingEnquiryRepository;
import com.example.MpApp.repository.telecallerstaff.TelecallingFollowupRepository;
import com.example.MpApp.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TelecallingService {

    private final TelecallingEnquiryRepository enquiryRepository;
    private final TelecallingFollowupRepository followupRepository;
    private final OfficeStaffRepository officeStaffRepository;
    private final StudentRepository studentRepository;
    private final JwtService jwtService;

    /*
     =====================================
     TELECALLER VALIDATION
     =====================================
     */

    public String extractEmail(String authHeader){
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new RuntimeException("Token Required");
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);

        return email;
    }

    private OfficeStaff validateTelecaller(Long staffId) {
        OfficeStaff staff = officeStaffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Office staff not found"));

        if (staff.getCategory() != StaffCategory.TELECOM_SERVICE) {
            throw new IllegalArgumentException("Only TELECOM_SERVICE staff can access telecalling module");
        }

        return staff;
    }

    /*
     =====================================
     CREATE ENQUIRY (UPDATED RESPONSE)
     =====================================
     */

    @Transactional
    public Map<String, String> createEnquiry(String authHeader, TelecallingEnquiryRequest request) {

        String email = extractEmail(authHeader);

        OfficeStaff staff = officeStaffRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Staff member not found.")
        );

        validateTelecaller(staff.getId());




        TelecallingEnquiry enquiry = new TelecallingEnquiry();

        enquiry.setStudentName(request.getStudentName());
        enquiry.setPhone(request.getPhone());
        enquiry.setEmail(request.getEmail());
        enquiry.setCollegeName(request.getCollegeName());
        enquiry.setDepartment(request.getDepartment());
        enquiry.setCity(request.getCity());
        enquiry.setDistrict(request.getDistrict());
        enquiry.setAddress(request.getAddress());
        enquiry.setInterestedCourse(request.getInterestedCourse());
        enquiry.setRemarks(request.getRemarks());
        enquiry.setStatus(EnquiryStatus.NEW);
        enquiry.setEnquiryDate(LocalDate.now());
        enquiry.setNextFollowupDate(request.getLatestFollowupDate());

        /* Link Student if exists */
        studentRepository.findByEmail(request.getEmail())
                .ifPresent(enquiry::setStudent);

        TelecallingEnquiry savedEnquiry = enquiryRepository.save(enquiry);

        Map<String, String> response = new HashMap<>();
        response.put("enquiryId", savedEnquiry.getId().toString());
        response.put("status", savedEnquiry.getStatus().toString());
        response.put("message", "Telecalling Enquiry Profile Stored Successfully");
        return response;
    }

    /*
     =====================================
     GET ALL ENQUIRIES (UNTOUCHED)
     =====================================
     */

    public List<TelecallingEnquiry> getAllEnquiries(String authHeader) {
        String email = extractEmail(authHeader);
        OfficeStaff staff = officeStaffRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Staff member not found.")
        );


        validateTelecaller(staff.getId());
        return enquiryRepository.findAll();
    }

    /*
     =====================================
     GET ENQUIRY BY ID (UNTOUCHED)
     =====================================
     */

    public TelecallingEnquiry getEnquiryById(String authHeader, Long enquiryId) {

        String email = extractEmail(authHeader);

        OfficeStaff staff = officeStaffRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Staff member not found.")
        );
        validateTelecaller(staff.getId());
        return enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));
    }

    /*
     =====================================
     UPDATE ENQUIRY (UPDATED RESPONSE)
     =====================================
     */

    @Transactional
    public Map<String, String> updateEnquiry(String authHeader, Long enquiryId, TelecallingUpdateRequest request) {
        String email = extractEmail(authHeader);
        OfficeStaff staff = officeStaffRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Staff member not found.")
        );

        validateTelecaller(staff.getId());

        TelecallingEnquiry enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));

        if (request.getStudentName() != null) enquiry.setStudentName(request.getStudentName());
        if (request.getPhone() != null) enquiry.setPhone(request.getPhone());
        if (request.getEmail() != null) enquiry.setEmail(request.getEmail());
        if (request.getCollegeName() != null) enquiry.setCollegeName(request.getCollegeName());
        if (request.getDepartment() != null) enquiry.setDepartment(request.getDepartment());
        if (request.getCity() != null) enquiry.setCity(request.getCity());
        if (request.getInterestedCourse() != null) enquiry.setInterestedCourse(request.getInterestedCourse());
        if (request.getStatus() != null) enquiry.setStatus(request.getStatus());
        if (request.getRemarks() != null) enquiry.setRemarks(request.getRemarks());
        if (request.getFollowupDate() != null) enquiry.setNextFollowupDate(request.getFollowupDate());

        System.out.println(
                enquiry.getNextFollowupDate()
        );


        enquiryRepository.save(enquiry);

        Map<String, String> response = new HashMap<>();
        response.put("enquiryId", enquiryId.toString());
        response.put("message", "Enquiry Information Altered Successfully");
        return response;
    }

    /*
     =====================================
     DELETE ENQUIRY (UNTOUCHED)
     =====================================
     */

    @Transactional
    public void deleteEnquiry(String authHeader, Long enquiryId) {
        String email = extractEmail(authHeader);
        OfficeStaff staff = officeStaffRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Staff member not found.")
        );
        validateTelecaller(staff.getId());
        if (!enquiryRepository.existsById(enquiryId)) {
            throw new ResourceNotFoundException("Enquiry instance missing for ID: " + enquiryId);
        }
        enquiryRepository.deleteById(enquiryId);
    }

    /*
     =====================================
     FILTERS (UNTOUCHED)
     =====================================
     */

    public List<TelecallingEnquiry> filterByCollege(String authHeader, String collegeName) {
        String email = extractEmail(authHeader);
        OfficeStaff staff = officeStaffRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Staff member not found.")
        );
        validateTelecaller(staff.getId());
        return enquiryRepository.findByCollegeNameContainingIgnoreCase(collegeName);
    }

    public List<TelecallingEnquiry> filterByStatus(String authHeader, EnquiryStatus status) {

        String email = extractEmail(authHeader);
        OfficeStaff staff = officeStaffRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Staff member not found.")
        );

        validateTelecaller(staff.getId());
        return enquiryRepository.findByStatus(status);
    }

    public List<TelecallingEnquiry> filterByStudentName(String authHeader, String studentName) {
        String email = extractEmail(authHeader);
        OfficeStaff staff = officeStaffRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Staff member not found.")
        );

        validateTelecaller(staff.getId());
        return enquiryRepository.findByStudentNameContainingIgnoreCase(studentName);
    }

    public List<TelecallingEnquiry> filterByDate(String authHeader, LocalDate date) {
        String email = extractEmail(authHeader);
        OfficeStaff staff = officeStaffRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Staff member not found.")
        );

        validateTelecaller(staff.getId());
        return enquiryRepository.findByEnquiryDate(date);
    }

    /*
     =====================================
     TODAY FOLLOWUPS (UNTOUCHED)
     =====================================
     */

    public List<TelecallingEnquiry> getTodayFollowups(String authHeader) {
        String email = extractEmail(authHeader);
        OfficeStaff staff = officeStaffRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Staff member not found.")
        );
        validateTelecaller(staff.getId());
        System.out.println(LocalDate.now());
        return enquiryRepository.findByNextFollowupDate(LocalDate.now());
    }

    public List<TelecallingFollowup> getOverdueFollowups(Long staffId) {
        validateTelecaller(staffId);
        return followupRepository.findByNextFollowupDateBefore(LocalDate.now());
    }

    /*
     =====================================
     CUSTOM FOLLOWUPS (UNTOUCHED)
     =====================================
     */

    public List<TelecallingFollowup> getCustomFollowups(String authHeader) {
        String email = extractEmail(authHeader);
        OfficeStaff staff = officeStaffRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Staff member not found.")
        );
        validateTelecaller(staff.getId());
        return followupRepository.findAll();
    }

    /*
     =====================================
     ADD FOLLOWUP (UPDATED RESPONSE)
     =====================================
     */

    @Transactional
    public Map<String, String> addFollowup(String authHeader, Long enquiryId, TelecallingFollowupRequest request) {
        OfficeStaff staff = officeStaffRepository.findByEmail(authHeader).orElseThrow(
                () -> new RuntimeException("Staff member not found.")
        );

        OfficeStaff telecaller = validateTelecaller(staff.getId());

        TelecallingEnquiry enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));

        TelecallingFollowup followup = new TelecallingFollowup();
        followup.setEnquiry(enquiry);
        followup.setStatus(request.getStatus());
        followup.setRemarks(request.getRemarks());
        followup.setNextFollowupDate(request.getNextFollowupDate());
        followup.setUpdatedBy(telecaller.getName());

        enquiry.setStatus(request.getStatus());

        enquiryRepository.save(enquiry);
        TelecallingFollowup savedFollowup = followupRepository.save(followup);

        Map<String, String> response = new HashMap<>();
        response.put("followupId", savedFollowup.getId().toString());
        response.put("enquiryId", enquiryId.toString());
        response.put("status", request.getStatus().toString());
        response.put("message", "Enquiry Followup Milestone Logged Successfully");
        return response;
    }

    /*
     =====================================
     FOLLOWUP HISTORY (UNTOUCHED)
     =====================================
     */

    public List<TelecallingFollowup> getFollowupHistory(String authHeader, Long enquiryId) {

        String email = extractEmail(authHeader);
        OfficeStaff staff = officeStaffRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Staff member not found.")
        );
        validateTelecaller(staff.getId());
        TelecallingEnquiry enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));
        return null;
    }

    /*
     =====================================
     CONVERT ENQUIRY TO STUDENT (UPDATED RESPONSE)
     =====================================
     */

    @Transactional
    public Map<String, String> updateEnquiryStatus(EnquiryStatus status,String authHeader) {
        String email = extractEmail(authHeader);
        OfficeStaff staff = officeStaffRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Staff member not found.")
        );
        TelecallingEnquiry enquiry = enquiryRepository.findById(staff.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));

        enquiry.setStatus(status);
        enquiryRepository.save(enquiry);

        Map<String, String> response = new HashMap<>();
        response.put("enquiryId", staff.getId().toString());
        response.put("status", status.toString());
        response.put("message", "Enquiry Status State Evaluated Successfully");
        return response;
    }
}