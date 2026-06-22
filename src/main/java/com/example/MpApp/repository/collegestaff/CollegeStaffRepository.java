package com.example.MpApp.repository.collegestaff;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.MpApp.entity.collegestaff.CollegeStaff;

public interface CollegeStaffRepository
        extends JpaRepository<CollegeStaff, Long> {

    Optional<CollegeStaff> findByEmail(
            String email);

    boolean existsByEmail(
            String email);

    boolean existsByMobileNumber(
            String mobileNumber);
}