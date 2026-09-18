package com.example.MpApp.repository.officestaff;

import com.example.MpApp.entity.officestaff.OfficeStaffAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OfficeStaffAttendanceRepository extends JpaRepository<OfficeStaffAttendance, Long> {
    Optional<OfficeStaffAttendance> findByStaffIdAndAttendanceDate(Long staffId, LocalDate date);
    List<OfficeStaffAttendance> findByStaffId(Long staffId);
    boolean existsByAttendanceDate(LocalDate date);
    List<OfficeStaffAttendance> findByAttendanceDate(LocalDate date);
}