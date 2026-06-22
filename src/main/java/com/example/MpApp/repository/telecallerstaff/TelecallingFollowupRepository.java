package com.example.MpApp.repository.telecallerstaff;

import com.example.MpApp.entity.enums.EnquiryStatus;
import com.example.MpApp.entity.telecallerstaff.TelecallingFollowup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TelecallingFollowupRepository
        extends JpaRepository<TelecallingFollowup, Long> {

    // All followups created on a specific date
    List<TelecallingFollowup> findByFollowupDate(LocalDate followupDate);

    // Exact next followup date (old method - keep if needed)
    List<TelecallingFollowup> findByNextFollowupDate(LocalDate nextFollowupDate);

    // Status based filter
    List<TelecallingFollowup> findByStatus(EnquiryStatus status);

    // Range filter (useful for reports)
    List<TelecallingFollowup> findByFollowupDateBetween(
            LocalDate startDate,
            LocalDate endDate);

    // 🔥 NEW: TODAY + UPCOMING FOLLOWUPS (IMPORTANT)
    List<TelecallingFollowup> findByNextFollowupDateGreaterThanEqual(
            LocalDate date);

    // 🔥 NEW: OVERDUE FOLLOWUPS
    List<TelecallingFollowup> findByNextFollowupDateBefore(
            LocalDate date);
}