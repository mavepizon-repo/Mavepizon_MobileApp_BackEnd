package com.example.MpApp.service.developer_trainer;

import com.example.MpApp.dto.developer_trainer_staff.*;
import com.example.MpApp.entity.developer_trainer_staff.*;
import com.example.MpApp.entity.enums.StaffCategory;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.repository.developer_trainer.*;
import com.example.MpApp.repository.officestaff.OfficeStaffRepository;
import com.example.MpApp.repository.student.StudentRepository;
import com.example.MpApp.exception.ResourceNotFoundException;

import com.example.MpApp.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeveloperTrainerService {

    private final OfficeStaffRepository officeStaffRepository;
    private final TrainingBatchRepository batchRepository;
    private final StudentRepository studentRepository;
    private final StudentAttendanceRepository attendanceRepository;
    private final CourseMaterialRepository materialRepository;
    private final CashFeeConfirmationRepository feeConfirmationRepository;

    private final CloudinaryService cloudinaryService;

    @Value("${file.upload-dir.course-materials:uploads/materials/}")
    private String uploadDir;

    private OfficeStaff validateTrainer(Long staffId) {
        OfficeStaff staff = officeStaffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found for ID: " + staffId));

        if (staff.getCategory() != StaffCategory.DEVELOPER_TRAINER
                && !"TRAINER".equalsIgnoreCase(staff.getRole())) {
            throw new IllegalArgumentException("Only TRAINER or DEVELOPER_TRAINER can access this service module");
        }
        return staff;
    }

    /* ================= BATCH MANAGEMENT (UNTOUCHED) ================= */
    public List<BatchDTO> getAssignedBatches(Long staffId) {
        validateTrainer(staffId);
        return batchRepository.findByTrainerIdWithDetails(staffId);
    }

    public List<BatchDTO> getOnlineBatches(Long staffId) {
        validateTrainer(staffId);
        return batchRepository.findByTrainerIdWithDetails(staffId).stream()
                .filter(b -> "ONLINE".equalsIgnoreCase(b.getBatchMode()))
                .collect(Collectors.toList());
    }

    public List<BatchDTO> getOfflineBatches(Long staffId) {
        validateTrainer(staffId);
        return batchRepository.findByTrainerIdWithDetails(staffId).stream()
                .filter(b -> "OFFLINE".equalsIgnoreCase(b.getBatchMode()))
                .collect(Collectors.toList());
    }

    /* ================= ATTENDANCE (UPDATED RESPONSE) ================= */
    @Transactional
    public Map<String, String> markAttendance(Long staffId, AttendanceRequest request) {
        validateTrainer(staffId);
        TrainingBatch batch = batchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found for ID: " + request.getBatchId()));

        List<StudentAttendance> records = request.getStudents().stream().map(dto -> {
            Student s = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found for ID: " + dto.getStudentId()));
            StudentAttendance a = new StudentAttendance();
            a.setBatch(batch);
            a.setStudent(s);
            a.setAttendanceDate(request.getDate() != null ? request.getDate() : LocalDate.now());
            a.setPresent(dto.isPresent());
            return a;
        }).collect(Collectors.toList());

        attendanceRepository.saveAll(records);

        Map<String, String> response = new HashMap<>();
        response.put("batchId", batch.getId().toString());
        response.put("totalRecordsMarked", String.valueOf(records.size()));
        response.put("message", "Student Attendance Logs Stored Successfully");
        return response;
    }

    /* ================= MATERIAL UPLOAD (UPDATED RESPONSE) ================= */
    @Transactional
    public Map<String, String> uploadMaterial(Long staffId, Long batchId, String title, MultipartFile file) {
        OfficeStaff trainer = validateTrainer(staffId);
        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Target batch context not found for ID: " + batchId));

        // Guard against empty form payloads
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot process request. Uploaded file attachment is empty.");
        }

        // Changed subFolder destination from "courseMaterial" to "material"
        String secureUrl = cloudinaryService.uploadFile(file, "material");

        CourseMaterial m = new CourseMaterial();
        m.setBatch(batch);
        m.setTrainer(trainer);
        m.setTitle(title);
        m.setFileUrl(secureUrl); // Stores the secure https url pointing to your new folder structure

        CourseMaterial saved = materialRepository.save(m);

        Map<String, String> response = new HashMap<>();
        response.put("materialId", saved.getId().toString());
        response.put("batchId", batchId.toString());
        response.put("fileUrl", secureUrl);
        response.put("message", "Course Study Material Uploaded Successfully to material folder");
        return response;
    }

    /* ================= ZOOM LINK (UPDATED RESPONSE) ================= */
    @Transactional
    public Map<String, String> updateZoomLink(Long staffId, ZoomLinkRequest request) {
        validateTrainer(staffId);
        TrainingBatch b = batchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Training batch mapping not found for ID: " + request.getBatchId()));

        b.setZoomLink(request.getZoomLink());
        batchRepository.save(b);

        Map<String, String> response = new HashMap<>();
        response.put("batchId", b.getId().toString());
        response.put("message", "Batch Streaming Coordinates Updated Successfully");
        return response;
    }


    /* ================= FEES CONFIRMATION (UPDATED RESPONSE) ================= */
    @Transactional
    public Map<String, String> confirmFees(Long staffId, FeeConfirmationRequest request) {
        OfficeStaff trainer = validateTrainer(staffId);
        TrainingBatch batch = batchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch registration trace missing for ID: " + request.getBatchId()));
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student target validation entry missing for ID: " + request.getStudentId()));

        CashFeeConfirmation c = new CashFeeConfirmation();
        c.setTrainer(trainer);
        c.setBatch(batch);
        c.setStudent(student);
        c.setAmount(request.getAmount());
        c.setRemarks(request.getRemarks());
        c.setStatus("PENDING");

        CashFeeConfirmation saved = feeConfirmationRepository.save(c);

        Map<String, String> response = new HashMap<>();
        response.put("confirmationId", saved.getId().toString());
        response.put("studentId", student.getId().toString());
        // String cast values to capture metadata cleanly
        response.put("amount", String.valueOf(saved.getAmount()));
        response.put("status", "PENDING");
        response.put("message", "Cash Collection Invoice Forwarded For Approval");
        return response;
    }

    /* ================= DASHBOARD CORE METRICS (UNTOUCHED) ================= */
    public Map<String, Object> getDashboard(Long staffId) {
        validateTrainer(staffId);
        Map<String, Object> dashboard = new HashMap<>();
        List<BatchDTO> assignedBatches = batchRepository.findByTrainerIdWithDetails(staffId);

        dashboard.put("assignedBatches", assignedBatches.size());
        dashboard.put("onlineBatches", assignedBatches.stream().filter(b -> "ONLINE".equalsIgnoreCase(b.getBatchMode())).count());
        dashboard.put("offlineBatches", assignedBatches.stream().filter(b -> "OFFLINE".equalsIgnoreCase(b.getBatchMode())).count());

        long attendanceToday = assignedBatches.stream()
                .map(BatchDTO::getBatchId)
                .flatMap(id -> attendanceRepository.findByBatchId(id).stream())
                .filter(att -> LocalDate.now().equals(att.getAttendanceDate()))
                .count();

        dashboard.put("attendanceToday", attendanceToday);
        return dashboard;
    }

    // Add this to your injected dependencies at the top of DeveloperTrainerService
    private final BatchStudentRepository batchStudentRepository;

    /* ================= GET BATCH STUDENTS (OPTIMIZED) ================= */
    public List<BatchStudentDTO> getBatchStudents(Long staffId, Long batchId) {
        validateTrainer(staffId);

        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Training batch not found for ID: " + batchId));

        // Security check: Ensure this trainer actually owns this batch
        if (batch.getTrainer() == null || !batch.getTrainer().getId().equals(staffId)) {
            throw new IllegalArgumentException("Access Denied: You are not assigned to this training batch.");
        }

        // Returns the mappings with the Student objects pre-fetched
        return batchStudentRepository.findBatchStudentsFlat(batchId);
    }
}