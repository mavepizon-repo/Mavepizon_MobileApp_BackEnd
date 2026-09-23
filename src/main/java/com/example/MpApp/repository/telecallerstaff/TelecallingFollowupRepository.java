package com.example.MpApp.repository.telecallerstaff;

import com.example.MpApp.entity.enums.EnquiryStatus;
import com.example.MpApp.entity.telecallerstaff.TelecallingFollowup;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TelecallingFollowupRepository
        extends JpaRepository<TelecallingFollowup, Long> {

    // All followups created on a specific date
    List<TelecallingFollowup> findByFollowupDate(LocalDate followupDate);

    // Exact next followup date
    List<TelecallingFollowup> findByNextFollowupDate(
            LocalDate nextFollowupDate);

    // Status based filter
    List<TelecallingFollowup> findByStatus(
            EnquiryStatus status);

    // Range filter
    List<TelecallingFollowup> findByFollowupDateBetween(
            LocalDate startDate,
            LocalDate endDate);

    // TODAY + UPCOMING FOLLOWUPS
    List<TelecallingFollowup> findByNextFollowupDateGreaterThanEqual(
            LocalDate date);

    // OVERDUE FOLLOWUPS
    List<TelecallingFollowup> findByNextFollowupDateBefore(
            LocalDate date);

    // Staff-specific overdue followups
    List<TelecallingFollowup> findByEnquiryStaffIdAndNextFollowupDateBefore(
            Long staffId,
            LocalDate date);

    // Staff-specific followups with pagination
    Page<TelecallingFollowup> findAllByEnquiryStaffId(
            Long staffId,
            Pageable pageable);

    // Followup history for a specific enquiry owned by the staff
    List<TelecallingFollowup> findByEnquiryIdAndEnquiryStaffId(
            Long enquiryId,
            Long staffId);

    // Get all followup history for an enquiry
    List<TelecallingFollowup> findByEnquiryId(
            Long enquiryId);
}