package com.example.MpApp.service.student;

import com.example.MpApp.security.PasswordPolicy;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.exception.InvalidCredentialsException;
import com.example.MpApp.exception.OtpException;
import com.example.MpApp.dto.file.FileViewResponse;
import com.example.MpApp.dto.student.StudentLoginRequest;
import com.example.MpApp.dto.student.StudentRegisterRequest;
import com.example.MpApp.entity.course.StudentCourseRegistration;
import com.example.MpApp.entity.student.Notification;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.repository.course.StudentCourseRegistrationRepository;
import com.example.MpApp.repository.student.NotificationRepository;
import com.example.MpApp.repository.student.StudentRepository;
import com.example.MpApp.service.CloudinaryService;
import com.example.MpApp.service.otp.OtpService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.security.SecureRandom;

@Service
public class StudentService {

    @Autowired
    private StudentRepository repository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CloudinaryService cloudinaryService;

    // Course registration and student-related repositories
    @Autowired
    private StudentCourseRegistrationRepository registrationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private OtpService otpService;

    public Map<String, Object> getStudentDashboard(Long studentId) {
        Student student = repository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student Not Found"));

        Map<String, Object> dashboard = new HashMap<>();

        // 1. Basic Info
        dashboard.put("name", student.getName());
        dashboard.put("studentId", student.getStudentId());

        // 2. Course Statistics (using the repository method that takes Long)
        List<StudentCourseRegistration> registrations =
                registrationRepository.findByStudentStudentId(
                        student.getStudentId()
                );
        dashboard.put("enrolledCoursesCount", registrations != null ? registrations.size() : 0);


        // 4. Notifications (using the JpaRepository standard findByStudentId)
        List<Notification> notifications =  notificationRepository.findByStudentId(studentId);
        long unreadCount = (notifications != null) ? notifications.stream()
                .filter(n -> !n.isRead())
                .count() : 0;

        dashboard.put("unreadNotifications", unreadCount);

        return dashboard;
    }

    private String generateStudentId() {

        LocalDate today = LocalDate.now();

        String month =
                String.format("%02d",
                        today.getMonthValue());

        String year =
                String.valueOf(today.getYear())
                        .substring(2);

        String prefix =
                "MPST" + month + year;

        long count = repository.nextBusinessId();

        return prefix +
                String.format("%03d", count);
    }

    // ================= STUDENT REGISTER =================
    public Student registerStudent(StudentRegisterRequest request) {

        PasswordPolicy.validate(request.getPassword());

        if (repository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email Already Exists");
        }

        if (repository.existsByMobileNumber(request.getMobileNumber())) {
            throw new RuntimeException("Phone Number Already Exists");
        }

        Student student = new Student();

        String studentId = generateStudentId();

        student.setStudentId(studentId);
        student.setName(request.getName());
        student.setGender(request.getGender());
        student.setEmail(request.getEmail());
        student.setMobileNumber(request.getMobileNumber());

        student.setPassword(
                passwordEncoder.encode(
                        request.getPassword()));

        student.setCollegeName(
                request.getCollegeName());

        student.setDepartment(
                request.getDepartment());

        student.setSkills(
                request.getSkills());

        return repository.save(student);
    }

    public Student updateStudentFiles(Long id, MultipartFile profile) {
        Student student = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student Not Found"));

        // Fix: Guard against empty multipart form payloads
        if (profile != null && !profile.isEmpty()) {
            student.setProfilePhoto(cloudinaryService.uploadFile(profile, "student/profile"));
        }

        return repository.save(student);
    }

    // 🔍 RETRIEVAL ACTION
    public FileViewResponse getStudentFiles(Long id) {
        Student student = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student Not Found"));
        return new FileViewResponse(student.getProfilePhoto(),null,null);
    }

    // ================= STUDENT LOGIN =================
    public Map<String, String> loginStudent(StudentLoginRequest request) {

        Student student = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), student.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // ✅ Create UserDetails object
        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username(student.getEmail())
                        .password(student.getPassword())
                        .roles("STUDENT")
                        .build();

        // ✅ Generate token using UserDetails
        String token = jwtService.generateToken(userDetails, student.getTokenVersion());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", "STUDENT");
        response.put("studentId", student.getStudentId());
        response.put("email", student.getEmail());
        response.put("message", "Login Successful");

        return response;
    }

    // ================= STUDENT LOGOUT =================
    public String logout() {
        return "Logout Successful";
    }

    // ================= FORGOT PASSWORD =================

    private static final String ROLE = "STUDENT";

    /**
     * Always acknowledges the request, whether or not the address is registered.
     * A distinct "Email Not Found" would let anyone enumerate student accounts.
     */
    public String sendOtp(String email) {
        return otpService.issueOtp(email, ROLE, repository.findByEmail(email).isPresent());
    }

    public void verifyOtp(String email, String otp) {
        otpService.verifyOtp(email, ROLE, otp);
    }

    @Transactional
    public String resetPassword(String email, String otp, String newPassword) {
        // Validate before consuming, so the code survives a rejected password.
        PasswordPolicy.validate(newPassword);

        // Consumes the code, so it cannot be replayed for a second reset.
        otpService.consumeVerifiedOtp(email, ROLE, otp);

        Student student = repository.findByEmail(email)
                .orElseThrow(() -> new OtpException("OTP_NOT_REQUESTED",
                        "No OTP has been requested for this account. Please request an OTP first."));

        student.setPassword(passwordEncoder.encode(newPassword));

        // Revoke every token issued before this reset. Without this the previous
        // holder of a leaked token keeps access for the full 24h expiry.
        student.setTokenVersion(student.getTokenVersion() + 1);

        repository.save(student);
        return "Password Reset Successful";
    }

    // ================= UPDATE STUDENT PROFILE =================
    public Student updateProfile(Long id, Student updated) {

        Student student = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student Not Found"));

        if (updated.getName() != null)
            student.setName(updated.getName());

        if (updated.getGender() != null)
            student.setGender(updated.getGender());

        if (updated.getCollegeName() != null)
            student.setCollegeName(updated.getCollegeName());

        if (updated.getDepartment() != null)
            student.setDepartment(updated.getDepartment());

        if (updated.getMobileNumber() != null)
            student.setMobileNumber(updated.getMobileNumber());

        if (updated.getSkills() != null)
            student.setSkills(updated.getSkills());

        if (updated.getProfilePhoto() != null)
            student.setProfilePhoto(updated.getProfilePhoto());

        return repository.save(student);
    }

    @Transactional
    public String changePassword(Long studentId, String oldPassword, String newPassword) {
        Student student = repository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // 1. Verify old password
        if (!passwordEncoder.matches(oldPassword, student.getPassword())) {
            throw new RuntimeException("Invalid Old Password");
        }

        // 2. Prevent reuse of old password
        if (passwordEncoder.matches(newPassword, student.getPassword())) {
            throw new IllegalStateException("New password cannot be the same as your old password.");
        }

        // 3. Encrypt and save
        PasswordPolicy.validate(newPassword);
        student.setPassword(passwordEncoder.encode(newPassword));

        // Invalidate tokens issued under the old password.
        student.setTokenVersion(student.getTokenVersion() + 1);

        repository.save(student);

        return "Password Changed Successfully";
    }

    public Map<String, Object> getStudentDashboardByEmail(String email) {
        Student student = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student Not Found"));
        return getStudentDashboard(student.getId());
    }

    public Student updateStudentFilesByEmail(String email, MultipartFile profile) {
        Student student = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student Not Found"));
        if (profile != null && !profile.isEmpty()) {
            student.setProfilePhoto(cloudinaryService.uploadFile(profile, "student/profile"));
        }
        return repository.save(student);
    }

    public FileViewResponse getStudentFilesByEmail(String email) {
        Student student = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student Not Found"));
        return new FileViewResponse(student.getProfilePhoto(), null, null);
    }

    public Student updateProfileByEmail(String email, Student updated) {
        Student student = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student Not Found"));

        if (updated.getName() != null) student.setName(updated.getName());
        if (updated.getGender() != null) student.setGender(updated.getGender());
        if (updated.getCollegeName() != null) student.setCollegeName(updated.getCollegeName());
        if (updated.getDepartment() != null) student.setDepartment(updated.getDepartment());
        if (updated.getMobileNumber() != null) student.setMobileNumber(updated.getMobileNumber());
        if (updated.getSkills() != null) student.setSkills(updated.getSkills());
        if (updated.getProfilePhoto() != null) student.setProfilePhoto(updated.getProfilePhoto());

        return repository.save(student);
    }

    @Transactional
    public String changePasswordByEmail(String email, String oldPassword, String newPassword) {
        Student student = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student Not Found"));

        if (!passwordEncoder.matches(oldPassword, student.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        PasswordPolicy.validate(newPassword);
        student.setPassword(passwordEncoder.encode(newPassword));

        // Invalidate tokens issued under the old password.
        student.setTokenVersion(student.getTokenVersion() + 1);

        repository.save(student);
        return "Password Changed Successfully";
    }

}
