package com.example.MpApp.service.student;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.dto.file.FileViewResponse;
import com.example.MpApp.dto.student.StudentLoginRequest;
import com.example.MpApp.dto.student.StudentRegisterRequest;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.repository.student.StudentRepository;
import com.example.MpApp.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

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

    // ================= OTP STORAGE =================
    private final Map<String, String> otpStorage = new HashMap<>();

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

        long count =
                repository.count() + 1;

        return prefix +
                String.format("%03d", count);
    }

    // ================= REGISTER =================
    public Student registerStudent(StudentRegisterRequest request) {

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

        // Fix: Guard against empty multi-part form payloads
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

    // ================= LOGIN =================
    public Map<String, String> loginStudent(StudentLoginRequest request) {

        Student student = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email Not Found"));

        if (!passwordEncoder.matches(request.getPassword(), student.getPassword())) {
            throw new RuntimeException("Invalid Password");
        }

        // ✅ Create UserDetails object
        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username(student.getEmail())
                        .password(student.getPassword())
                        .roles("STUDENT")
                        .build();

        // ✅ Generate token using UserDetails
        String token = jwtService.generateToken(userDetails);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("studentId", student.getStudentId());
        response.put("email", student.getEmail());
        response.put("message", "Login Successful");

        return response;
    }

    // ================= LOGOUT =================
    public String logout() {
        return "Logout Successful";
    }

    // ================= FORGOT PASSWORD =================

    public String sendOtp(String email) {

        Student student = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email Not Found"));

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        otpStorage.put(email, otp);

        System.out.println("OTP for " + email + " is: " + otp);

        return "OTP sent successfully";
    }

    public String verifyOtp(String email, String otp) {

        if (!otpStorage.containsKey(email)) {
            throw new RuntimeException("OTP not requested");
        }

        if (!otpStorage.get(email).equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        return "OTP Verified Successfully";
    }

    public String resetPassword(String email, String otp, String newPassword) {

        if (!otpStorage.containsKey(email)) {
            throw new RuntimeException("OTP not requested");
        }

        if (!otpStorage.get(email).equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        Student student = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email Not Found"));

        student.setPassword(passwordEncoder.encode(newPassword));

        repository.save(student);

        otpStorage.remove(email);

        return "Password Reset Successful";
    }

    // ================= UPDATE PROFILE =================
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


}