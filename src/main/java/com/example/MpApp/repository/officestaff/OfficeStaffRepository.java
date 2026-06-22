package com.example.MpApp.repository.officestaff;

import java.util.List;
import java.util.Optional;

import com.example.MpApp.entity.enums.StaffCategory;
import com.example.MpApp.entity.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.MpApp.entity.officestaff.OfficeStaff;

public interface OfficeStaffRepository
        extends JpaRepository<OfficeStaff, Long> {

    Optional<OfficeStaff> findByEmail(
            String email);
    long countByBranchAndCategory(
            String branch,
            StaffCategory category);
}