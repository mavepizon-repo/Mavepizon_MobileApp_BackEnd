package com.example.MpApp.service.certificate;

import com.example.MpApp.dto.certificate.CertificateDTO;
import com.example.MpApp.entity.certificate.Certificate;
import com.example.MpApp.entity.course.StudentCourseRegistration;
import com.example.MpApp.entity.developer_trainer_staff.TrainingBatch;
import com.example.MpApp.repository.certificate.CertificateRepository;
import com.example.MpApp.repository.course.StudentCourseRegistrationRepository;
import com.example.MpApp.repository.developer_trainer.TrainingBatchRepository;
import com.example.MpApp.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CertificateService {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private TrainingBatchRepository batchRepository;

    @Autowired
    private StudentCourseRegistrationRepository courseRegistrationRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    // ================= 1. BULK INITIATE BY BATCH (For Trainers) =================
    @Transactional
    public Map<String, String> initiateBatchCertificates(Long batchId) {
        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Training Batch not found for ID: " + batchId));

        // Use our new bridge query!
        List<StudentCourseRegistration> registrations = courseRegistrationRepository.findRegistrationsByBatchId(batchId);

        if (registrations.isEmpty()) {
            throw new RuntimeException("No valid student course registrations found for this batch.");
        }

        List<Certificate> certificatesToSave = new ArrayList<>();

        for (StudentCourseRegistration reg : registrations) {
            Certificate cert = new Certificate();
            cert.setStudent(reg.getStudent());
            cert.setTrainingBatch(batch);
            cert.setCourseRegistration(reg);
            cert.setRecordType("COURSE");
            cert.setStatus("PENDING");

            certificatesToSave.add(cert);
        }

        certificateRepository.saveAll(certificatesToSave);

        return Map.of(
                "message", "Successfully queued " + certificatesToSave.size() + " PENDING certificates for batch: " + batch.getBatchName()
        );
    }

    // ================= 2. UPLOAD FILE (For Admins/Designers) =================
    @Transactional
    public Map<String, String> uploadCertificateFile(Long certificateId, MultipartFile file) {
        Certificate cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate record not found"));

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty or missing");
        }

        String fileUrl = cloudinaryService.uploadFile(file, "certificates");

        cert.setFileUrl(fileUrl);
        cert.setStatus("ISSUED");
        cert.setIssueDate(LocalDate.now());

        // Update the registration status back to GENERATED
        if (cert.getCourseRegistration() != null) {
            cert.getCourseRegistration().setCertificateStatus("GENERATED");
            courseRegistrationRepository.save(cert.getCourseRegistration());
        }

        certificateRepository.save(cert);

        return Map.of(
                "message", "Certificate file uploaded and marked as ISSUED.",
                "fileUrl", fileUrl
        );
    }

    // ================= 3. GETTERS =================
    // ================= 3. GETTERS (UPDATED TO DTO) =================
    public List<CertificateDTO> getPendingCertificates() {
        return certificateRepository.findByStatusIgnoreCaseFlat("PENDING");
    }

    public List<CertificateDTO> getCertificatesByBatch(Long batchId) {
        return certificateRepository.findByTrainingBatchIdFlat(batchId);
    }

    public List<CertificateDTO> getStudentCertificates(Long studentId) {
        return certificateRepository.findByStudentIdFlat(studentId);
    }
}