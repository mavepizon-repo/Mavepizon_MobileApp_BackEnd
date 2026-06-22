package com.example.MpApp.service.collegestaff;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.dto.collegestaff.CollegeStaffLoginRequest;
import com.example.MpApp.entity.collegestaff.CollegeStaff;
import com.example.MpApp.repository.collegestaff.CollegeStaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
@Service
public class CollegeStaffService {

    @Autowired
    private CollegeStaffRepository repository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Map<String, String> loginCollegeStaff(CollegeStaffLoginRequest request) {

        CollegeStaff collegeStaff =
                repository.findByEmail(request.getEmail())
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

        // IMPORTANT: use global UserDetailsService indirectly via JwtService
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .builder()
                .username(collegeStaff.getEmail())
                .password(collegeStaff.getPassword())
                .roles("COLLEGE_STAFF")
                .build();

        String token = jwtService.generateToken(userDetails);

        response.put("token", token);
        response.put("email", collegeStaff.getEmail());
        response.put("name", collegeStaff.getName());
        response.put("message", "Login Successful");

        return response;
    }
}