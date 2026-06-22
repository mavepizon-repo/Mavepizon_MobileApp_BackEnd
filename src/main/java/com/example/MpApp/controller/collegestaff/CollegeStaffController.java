package com.example.MpApp.controller.collegestaff;

import com.example.MpApp.dto.collegestaff.CollegeStaffLoginRequest;
import com.example.MpApp.service.collegestaff.CollegeStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/collegestaff")
@CrossOrigin("*")
public class CollegeStaffController {

    @Autowired
    private CollegeStaffService service;

    @PostMapping("/login")
    public Map<String, String> loginCollegeStaff(
            @RequestBody CollegeStaffLoginRequest request) {

        return service.loginCollegeStaff(request);
    }

}