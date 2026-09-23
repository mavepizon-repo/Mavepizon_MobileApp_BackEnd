package com.example.MpApp.repository.telecallerstaff;

import com.example.MpApp.entity.enums.EnquiryStatus;
import com.example.MpApp.entity.telecallerstaff.TelecallingEnquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TelecallingEnquiryRepository
        extends JpaRepository<TelecallingEnquiry, Long> {

    Page<TelecallingEnquiry> findAllByStaffId(Long staffId, Pageable pageable);

    Optional<TelecallingEnquiry> findByIdAndStaffId(Long id, Long staffId);

    boolean existsByIdAndStaffId(Long id, Long staffId);

    List<TelecallingEnquiry> findByStaffIdAndCollegeNameContainingIgnoreCase(
            Long staffId, String collegeName);

    List<TelecallingEnquiry> findByStaffIdAndStatus(
            Long staffId, EnquiryStatus status);

    List<TelecallingEnquiry> findByStaffIdAndStudentNameContainingIgnoreCase(
            Long staffId, String studentName);

    List<TelecallingEnquiry> findByStaffIdAndEnquiryDate(
            Long staffId, LocalDate enquiryDate);

    List<TelecallingEnquiry> findByStaffIdAndNextFollowupDate(
            Long staffId, LocalDate nextFollowupDate);

    List<TelecallingEnquiry> findByStaffIdAndNextFollowupDateBefore(
            Long staffId, LocalDate nextFollowupDate);

    List<TelecallingEnquiry> findByStaffIdAndNextFollowupDateGreaterThanEqual(
            Long staffId, LocalDate nextFollowupDate);
}
