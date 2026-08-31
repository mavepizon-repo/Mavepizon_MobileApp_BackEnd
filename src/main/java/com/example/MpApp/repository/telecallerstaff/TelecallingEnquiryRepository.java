package com.example.MpApp.repository.telecallerstaff;

import com.example.MpApp.entity.enums.EnquiryStatus;
import com.example.MpApp.entity.telecallerstaff.TelecallingEnquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TelecallingEnquiryRepository
        extends JpaRepository<TelecallingEnquiry, Long> {

    List<TelecallingEnquiry> findByCollegeName(String collegeName);

    List<TelecallingEnquiry> findByStudentNameContainingIgnoreCase(
            String studentName);

    List<TelecallingEnquiry> findByStatus(
            EnquiryStatus status);

    List<TelecallingEnquiry> findByEnquiryDate(
            LocalDate enquiryDate);

    List<TelecallingEnquiry> findByEnquiryDateBetween(
            LocalDate startDate,
            LocalDate endDate);

    List<TelecallingEnquiry> findByCollegeNameContainingIgnoreCase(
            String collegeName);

    List<TelecallingEnquiry> findByStudentNameContainingIgnoreCaseAndStatus(
            String studentName,
            EnquiryStatus status);

    List<TelecallingEnquiry> findByCollegeNameContainingIgnoreCaseAndStatus(
            String collegeName,
            EnquiryStatus status);

    List<TelecallingEnquiry> findByNextFollowupDate(LocalDate nextFollowupDate);
}