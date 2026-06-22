package com.example.MpApp.repository.officestaff;

import com.example.MpApp.entity.officestaff.OfficeStaffLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OfficeStaffLeaveRepository extends JpaRepository<OfficeStaffLeave, Long> {

    // Forces an INNER JOIN to fetch the staff details alongside the leave records in 1 query
    @Query("SELECT l FROM OfficeStaffLeave l JOIN FETCH l.staff WHERE l.staff.id = :staffId")
    List<OfficeStaffLeave> findByStaffIdWithStaff(@Param("staffId") Long staffId);
}