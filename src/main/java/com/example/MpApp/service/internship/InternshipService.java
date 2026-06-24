package com.example.MpApp.service.internship;

import com.example.MpApp.entity.internship.Internship;
import com.example.MpApp.entity.internship.InternshipRegistration;
import com.example.MpApp.exception.ResourceNotFoundException;
import com.example.MpApp.repository.internship.InternshipRegistrationRepository;
import com.example.MpApp.repository.internship.InternshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InternshipService {

    private final InternshipRepository internshipRepository;
    private final InternshipRegistrationRepository registrationRepository;

    @Transactional
    public Map<String, Object> createInternship(Internship internship, String creatorRole) {

        // 1. Generate Custom Auto-Code passing Internship Name string context
        String generatedCode = generateAutoInternshipCode(internship.getInternshipName());
        internship.setInternshipCode(generatedCode);

        // 2. Synchronize Batch Tracking Tag
        internship.setBatchCode("B-" + generatedCode);

        internship.setRegistrationFees(internship.getFees()*0.3);

        // 3. Keep remaining assignments untouched
        internship.setCreatedBy(creatorRole);
        internship.setAvailableSeatsOnline(internship.getTotalSeatsOnline());
        internship.setAvailableSeatsOffline(internship.getTotalSeatsOffline());

        Internship saved = internshipRepository.save(internship);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Internship deployed successfully.");
        response.put("generatedCode", saved.getInternshipCode());
        response.put("batchCode", saved.getBatchCode());
        response.put("internshipId", saved.getId());
        return response;
    }

    /**
     * AUTO GENERATOR HELPER: Formats code as <INT><typeofintern><month><year><Mid_no>
     */
    private String generateAutoInternshipCode(String internshipName) {
        if (internshipName == null) {
            throw new IllegalArgumentException("Internship name cannot be null for code generation.");
        }

        String upperName = internshipName.toUpperCase();
        String typeOfIntern = "GEN"; // Default fallback type if no match found

        // 1. Map type of intern prefix string
        if (upperName.contains("FULL STACK")) {
            typeOfIntern = "FS";
        } else if (upperName.contains("FLUTTER")) {
            typeOfIntern = "FL";
        } else if (upperName.contains("SPRING BOOT")) {
            typeOfIntern = "SP";
        } else if (upperName.contains("PYTHON")) {
            typeOfIntern = "PDS";
        } else if (upperName.contains("BLOCKCHAIN")) {
            typeOfIntern = "BLC";
        }

        // 2. Extract Month & Year variables
        LocalDate today = LocalDate.now();
        String monthStr = String.format("%02d", today.getMonthValue()); // e.g., "06"
        int yearValue = today.getYear();                               // e.g., 2026

        // 3. Assemble base match filter: "INT" + Type + Month + Year
        String baseCodeMatchPrefix = "INT" + typeOfIntern + monthStr + yearValue;

        // 4. Determine next chronological row sequence count
        long existingCount = internshipRepository.countByInternshipCodePrefix(baseCodeMatchPrefix);
        long nextSequence = existingCount + 1;
        String midNoStr = String.format("%03d", nextSequence); // Formats to 3 digits: e.g., "001"

        // Result matches perfectly: INTFS062026001
        return baseCodeMatchPrefix + midNoStr;
    }

    @Transactional
    public Map<String, Object> updateInternship(Long id, Internship details) {
        Internship existing = internshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found for ID: " + id));

        existing.setInternshipName(details.getInternshipName());
        existing.setDescription(details.getDescription());
        existing.setDuration(details.getDuration());
        existing.setStartDate(details.getStartDate());
        existing.setEndDate(details.getEndDate());
        existing.setFees(details.getFees());
        existing.setRegistrationFees(details.getRegistrationFees());
        existing.setZoomLink(details.getZoomLink());
        existing.setTrainerName(details.getTrainerName());
        existing.setStatus(details.getStatus());

        Internship updated = internshipRepository.save(existing);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "UPDATED");
        response.put("message", "Internship records updated successfully.");
        response.put("internshipId", updated.getId());
        response.put("currentStatus", updated.getStatus());
        return response;
    }

    @Transactional
    public Map<String, Object> registerStudent(InternshipRegistration reg) {
        // Safe extraction of clean code from payload: e.g. "INT-01: Java" -> "INT-01"
        String code = reg.getInternshipCodeWithName().split(":")[0].trim();

        Internship internship = internshipRepository.findByInternshipCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("No active internship found matching code: " + code));

        if ("CLOSED".equalsIgnoreCase(internship.getStatus())) {
            throw new IllegalStateException("Registration closed: This batch is no longer accepting submissions.");
        }

        // Evaluate and decrement seats based on platform mode
        if (Boolean.TRUE.equals(reg.getIsOnline())) {
            if (internship.getAvailableSeatsOnline() <= 0) {
                throw new IllegalStateException("Seat inventory exhausted for Online batch allocations.");
            }
            internship.setAvailableSeatsOnline(internship.getAvailableSeatsOnline() - 1);
        } else {
            if (internship.getAvailableSeatsOffline() <= 0) {
                throw new IllegalStateException("Seat inventory exhausted for Offline batch allocations.");
            }
            internship.setAvailableSeatsOffline(internship.getAvailableSeatsOffline() - 1);
        }

        reg.setBatchCode(internship.getBatchCode());
        internshipRepository.save(internship);
        InternshipRegistration savedReg = registrationRepository.save(reg);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Student internship registration mapped cleanly.");
        response.put("registrationId", savedReg.getId());
        response.put("allocatedBatch", savedReg.getBatchCode());
        response.put("paymentStatus", savedReg.getPaymentStatus());
        response.put("certificateStatus", savedReg.getCertificateStatus());
        return response;
    }

    public List<Internship> getAllInternships() {
        return internshipRepository.findAll();
    }

    public Internship getInternshipById(Long id) {

        return internshipRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Internship not found with id: " + id
                        ));
    }

    @Transactional
    public void deleteInternship(Long id) {

        Internship internship =
                internshipRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Internship not found with id: " + id
                                ));

        internshipRepository.delete(internship);
    }
}