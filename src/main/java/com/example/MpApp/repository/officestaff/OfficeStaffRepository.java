package com.example.MpApp.repository.officestaff;

import java.util.List;
import java.util.Optional;

import com.example.MpApp.entity.enums.StaffCategory;
import com.example.MpApp.entity.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.MpApp.entity.officestaff.OfficeStaff;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OfficeStaffRepository
        extends JpaRepository<OfficeStaff, Long> {

    Optional<OfficeStaff> findByEmail(
            String email);
    long countByBranchAndCategory(
            String branch,
            StaffCategory category);

    List<OfficeStaff> findAllByOrderByScoreDesc();

    List<OfficeStaff> findByApprovalStatus(String status);

    List<OfficeStaff> findByBranch(String branch);

    @Query(value = "SELECT * FROM office_staff WHERE created_by_id = :id AND created_by_type = :type", nativeQuery = true)
    List<OfficeStaff> findByCreatedByIdAndType(@Param("id") Long id, @Param("type") String type);



}