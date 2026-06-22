package com.example.MpApp.service.course;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.dto.course.CashPaymentRequest;
import com.example.MpApp.entity.course.CashPayment;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.entity.course.StudentCourseRegistration;
import com.example.MpApp.entity.internship.InternshipRegistration;
import com.example.MpApp.repository.course.CashPaymentRepository;
import com.example.MpApp.repository.course.StudentCourseRegistrationRepository;
import com.example.MpApp.repository.internship.InternshipRegistrationRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffRepository;
import com.example.MpApp.repository.student.StudentRepository;
import com.example.MpApp.entity.enums.StaffCategory;
import com.example.MpApp.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CashPaymentService {

    @Autowired
    private CashPaymentRepository repository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentCourseRegistrationRepository registrationRepository;

    @Autowired
    private InternshipRegistrationRepository internshipRegistrationRepository;

    @Autowired
    private OfficeStaffRepository officeStaffRepository;

    @Autowired
    private JwtService jwtService;

    @Transactional
    public Map<String, String> createCashRequest(String token, CashPaymentRequest request) {
        String email = jwtService.extractEmail(token);

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student Not Found"));

        CashPayment payment = new CashPayment();
        payment.setStudent(student);
        payment.setAmount(request.getAmount());
        payment.setSelectedStaffId(request.getSelectedStaffId());
        payment.setSelectedStaffName(request.getSelectedStaffName());
        payment.setRemarks(request.getRemarks());
        payment.setStatus("PENDING");

        Map<String, String> response = new HashMap<>();

        // Handle Conditional Context Mapping Path
        if (request.getInternshipRegistrationId() != null) {
            InternshipRegistration internshipReg = internshipRegistrationRepository.findById(request.getInternshipRegistrationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Internship Registration Not Found"));

            payment.setInternshipRegistrationId(internshipReg.getId());
            response.put("internshipRegistrationId", internshipReg.getId().toString());
        } else if (request.getRegistrationId() != null) {
            StudentCourseRegistration courseReg = registrationRepository.findById(request.getRegistrationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course Registration Not Found"));

            payment.setRegistration(courseReg);
            response.put("registrationId", courseReg.getId().toString());
        } else {
            throw new IllegalArgumentException("Payment request must specify either a course or an internship registration tracking identifier.");
        }

        CashPayment savedPayment = repository.save(payment);

        response.put("paymentId", savedPayment.getId().toString());
        response.put("status", "PENDING");
        response.put("message", "Cash Payment Request Created Successfully");
        return response;
    }

    @Transactional
    public Map<String, String> approveCashPayment(Long paymentId, String token) {

        String email = jwtService.extractEmail(token);

        // Verify logged-in user is an Office Staff
        OfficeStaff staff = officeStaffRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Office Staff account not found"));

        // Only Developer Trainers can approve
        if (staff.getCategory() != StaffCategory.DEVELOPER_TRAINER) {
            throw new IllegalArgumentException(
                    "Only Developer Trainer staff can approve payments");
        }

        CashPayment payment = repository.findById(paymentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment Not Found"));

        // Verify assigned trainer
        if (!staff.getId().equals(payment.getSelectedStaffId())) {
            throw new IllegalArgumentException(
                    "You are not assigned to approve this payment");
        }

        if ("APPROVED".equalsIgnoreCase(payment.getStatus())) {
            throw new IllegalStateException(
                    "Payment Already Approved");
        }

        payment.setStatus("APPROVED");
        payment.setApprovedBy(staff.getName());
        payment.setApprovalDate(LocalDateTime.now());

        Map<String, String> response = new HashMap<>();

        // Internship Payment
        if (payment.getInternshipRegistrationId() != null) {

            InternshipRegistration internshipReg =
                    internshipRegistrationRepository
                            .findById(payment.getInternshipRegistrationId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Linked Internship Registration Not Found"));

            internshipReg.setPaymentStatus("PAID");
            internshipReg.setPaymentFor("Full fees");

            internshipRegistrationRepository.save(internshipReg);

            response.put(
                    "internshipRegistrationId",
                    internshipReg.getId().toString()
            );
        }

        // Course Payment
        else if (payment.getRegistration() != null) {

            StudentCourseRegistration courseReg =
                    payment.getRegistration();

            courseReg.setPaymentStatus("PAID");

            registrationRepository.save(courseReg);

            response.put(
                    "registrationId",
                    courseReg.getId().toString()
            );
        }

        repository.save(payment);

        response.put("paymentId", payment.getId().toString());
        response.put("status", "APPROVED");
        response.put("approvedBy", staff.getName());
        response.put("message", "Cash Payment Approved Successfully");

        return response;
    }

    @Transactional
    public Map<String, String> rejectCashPayment(
            Long paymentId,
            String remarks,
            String token) {

        String email = jwtService.extractEmail(token);

        OfficeStaff staff = officeStaffRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Office Staff account not found"));

        CashPayment payment = repository.findById(paymentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment Not Found"));

        if (!staff.getId().equals(payment.getSelectedStaffId())) {
            throw new IllegalArgumentException(
                    "You are not assigned to reject this payment");
        }

        if ("APPROVED".equalsIgnoreCase(payment.getStatus())) {
            throw new IllegalStateException(
                    "Approved Payment Cannot Be Rejected");
        }

        payment.setStatus("REJECTED");
        payment.setRemarks(remarks);
        payment.setApprovedBy(staff.getName());
        payment.setApprovalDate(LocalDateTime.now());

        Map<String, String> response = new HashMap<>();

        if (payment.getInternshipRegistrationId() != null) {

            InternshipRegistration internshipReg =
                    internshipRegistrationRepository
                            .findById(payment.getInternshipRegistrationId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Linked Internship Registration Not Found"));

            internshipReg.setPaymentStatus("UNPAID");

            internshipRegistrationRepository.save(internshipReg);

            response.put(
                    "internshipRegistrationId",
                    internshipReg.getId().toString()
            );
        }
        else if (payment.getRegistration() != null) {

            StudentCourseRegistration courseReg =
                    payment.getRegistration();

            courseReg.setPaymentStatus("UNPAID");

            registrationRepository.save(courseReg);

            response.put(
                    "registrationId",
                    courseReg.getId().toString()
            );
        }

        repository.save(payment);

        response.put("paymentId", paymentId.toString());
        response.put("status", "REJECTED");
        response.put("rejectedBy", staff.getName());
        response.put("message", "Cash Payment Rejected Successfully");

        return response;
    }

    // Keep read operations unmodified
    public List<CashPayment> getAllCashPayments() { return repository.findAllWithDetails(); }
    public CashPayment getCashPaymentById(Long id) { return repository.findPaymentDetailsById(id); }
    public List<CashPayment> getByStatus(String status) { return repository.findByStatusWithDetails(status); }
    public List<CashPayment> getByStaff(Long staffId) { return repository.findBySelectedStaffIdWithDetails(staffId); }

    public List<CashPayment> getMyPayments(String token) {
        String email = jwtService.extractEmail(token);
        Student student = studentRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Student Not Found"));
        return repository.findPaymentsByStudentId(student.getStudentId());
    }

    @Transactional
    public void deleteCashPayment(Long id) {
        if (!repository.existsById(id)) { throw new ResourceNotFoundException("Payment Not Found"); }
        repository.deleteById(id);
    }
}