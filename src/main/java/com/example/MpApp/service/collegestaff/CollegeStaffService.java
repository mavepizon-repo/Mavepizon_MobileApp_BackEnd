package com.example.MpApp.service.collegestaff;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.dto.collegestaff.CollegeStaffFileDTO;
import com.example.MpApp.dto.collegestaff.CollegeStaffLoginRequest;
import com.example.MpApp.entity.collegestaff.CollegeStaff;
import com.example.MpApp.entity.collegestaff.CollegeStaffFiles;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.repository.collegestaff.CollegeStaffFilesRepository;
import com.example.MpApp.repository.collegestaff.CollegeStaffRepository;
import com.example.MpApp.repository.student.StudentRepository;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CollegeStaffService {

    @Autowired
    private CollegeStaffRepository repository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CollegeStaffFilesRepository collegeStaffFilesRepository;

    // In-memory OTP storage
    private final Map<String, String> otpStorage = new HashMap<>();

    public String extractEmail(String authHeader){
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new RuntimeException("Token Required");
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);

        return email;
    }

    public Map<String, String> loginCollegeStaff(CollegeStaffLoginRequest request) {

        CollegeStaff collegeStaff = repository.findByEmail(request.getEmail())
                .orElse(null);

        Map<String, String> response = new HashMap<>();

        if (collegeStaff == null) {
            response.put("message", "Email Not Found");
            return response;
        }

        boolean matched = passwordEncoder.matches(
                request.getPassword(),
                collegeStaff.getPassword()
        );

        if (!matched) {
            response.put("message", "Invalid Password");
            return response;
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .builder()
                .username(collegeStaff.getEmail())
                .password(collegeStaff.getPassword())
                .roles("COLLEGE_STAFF")
                .build();

        String token = jwtService.generateToken(userDetails);

        response.put("token", token);
        response.put("id",String.valueOf(collegeStaff.getId()));
        response.put("email", collegeStaff.getEmail());
        response.put("name", collegeStaff.getName());
        response.put("message", "Login Successful");

        return response;
    }

    public List<CollegeStaffFileDTO> getAllFiles(String authHeader){
        String email = extractEmail(authHeader);

        CollegeStaff staff = repository.findByEmail(email).orElseThrow(() -> new RuntimeException("College Staff not found for Email: " + email));

        List<CollegeStaffFiles> files = collegeStaffFilesRepository.findByStaffId(staff.getId());


        List<CollegeStaffFileDTO> fileDTOs = new ArrayList<>();
        for (CollegeStaffFiles file : files) {
            CollegeStaffFileDTO fileDTO = new CollegeStaffFileDTO();
            fileDTO.setStaffId(file.getStaff().getId());
            fileDTO.setStaffName(file.getStaff().getName());
            fileDTO.setCollegeName(file.getStaff().getCollegeName());
            fileDTO.setSyllabusURL(file.getCourseURL());
            fileDTO.setProposalURL(file.getProposalURL());
            fileDTOs.add(fileDTO);

        }

        return fileDTOs;
    }

    // ================= FORGOT PASSWORD FLOW =================

    public String sendOtp(String email) {
        repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email Not Found"));

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        otpStorage.put(email, otp);
        System.out.println("OTP for College Staff (" + email + ") is: " + otp);

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

        CollegeStaff collegeStaff = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email Not Found"));

        collegeStaff.setPassword(passwordEncoder.encode(newPassword));
        repository.save(collegeStaff);

        otpStorage.remove(email);

        return "Password Reset Successful";
    }

    // ================= CHANGE PASSWORD FLOW =================

    public String changePassword(String email, String oldPassword, String newPassword) {
        CollegeStaff collegeStaff = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email Not Found"));

        if (!passwordEncoder.matches(oldPassword, collegeStaff.getPassword())) {
            throw new RuntimeException("Invalid Old Password");
        }

        collegeStaff.setPassword(passwordEncoder.encode(newPassword));
        repository.save(collegeStaff);

        return "Password Changed Successfully";
    }

    public CollegeStaff getProfile(Long id) {
        CollegeStaff staff= repository.findById(id)
                .orElseThrow(() -> new RuntimeException("College Staff not found for ID: " + id));

        staff.setPassword(null);
        return staff;
    }

    @Transactional
    public Map<String, Object> uploadStudentExcel(String authHeader, MultipartFile file) throws Exception {


        String email = extractEmail(authHeader);
        // 1. Validate File
        if (file.isEmpty()) {
            throw new RuntimeException("Please upload a valid Excel file.");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                && !contentType.equals("application/vnd.ms-excel"))) {
            throw new RuntimeException("Only .xlsx and .xls formats are supported.");
        }

        // 2. Fetch College Staff Context
        CollegeStaff staff = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("College Staff not found for Email: " + email));

        List<Student> studentsToSave = new ArrayList<>();
        int rowsProcessed = 0;

        // --- ID GENERATION SETUP ---
        java.time.LocalDate today = java.time.LocalDate.now();
        String month = String.format("%02d", today.getMonthValue());
        String year = String.valueOf(today.getYear()).substring(2);
        String prefix = "MPST" + month + year;

        // Fetch the current base count before the loop starts
        long currentDbCount = studentRepository.count();

        // 3. Parse Excel using Apache POI
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Iterate through rows (skipping header row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Ensure the row has an email before processing (Column 1)
                Cell emailCell = row.getCell(1);
                if (emailCell == null || getCellValueAsString(emailCell).isEmpty()) {
                    continue;
                }

                Student student = new Student();

                // --- AUTO-GENERATE STUDENT ID ---
                currentDbCount++; // Increment for each valid row
                String generatedStudentId = prefix + String.format("%03d", currentDbCount);
                student.setStudentId(generatedStudentId);

                // --- EXCEL COLUMNS ---
                // 0: Name, 1: Email, 2: Mobile Number, 3: Department, 4: Gender
                student.setName(getCellValueAsString(row.getCell(0)));
                student.setEmail(getCellValueAsString(row.getCell(1)).toLowerCase());
                student.setMobileNumber(getCellValueAsString(row.getCell(2)));
                student.setDepartment(getCellValueAsString(row.getCell(3)));
                student.setGender(getCellValueAsString(row.getCell(4)));

                // Set College Name automatically from the staff's profile
                student.setCollegeName(staff.getCollegeName());

                String rawPassword = getCellValueAsString(row.getCell(5));
                if (rawPassword.isEmpty()) {
                    rawPassword = "Student@123"; // Fallback if cell is empty
                }
                student.setPassword(passwordEncoder.encode(rawPassword));

                studentsToSave.add(student);
                rowsProcessed++;
            }
        }

        // 4. Save to Database
        if (!studentsToSave.isEmpty()) {
            studentRepository.saveAll(studentsToSave);

            // Update staff's upload count
            int existingCount = staff.getUploadedStudentsCount() != null ? staff.getUploadedStudentsCount() : 0;
            staff.setUploadedStudentsCount(existingCount + rowsProcessed);
            repository.save(staff);
        }

        // 5. Return Response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Excel parsed and students uploaded successfully.");
        response.put("studentsAdded", rowsProcessed);
        response.put("collegeName", staff.getCollegeName());

        return response;
    }

    // Helper method to extract cell values safely
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

}