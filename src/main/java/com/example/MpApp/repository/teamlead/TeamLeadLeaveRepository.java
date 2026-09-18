package com.example.MpApp.repository.teamlead;

import com.example.MpApp.entity.teamlead.TeamLeadLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamLeadLeaveRepository extends JpaRepository<TeamLeadLeave, Long> {

    // Find all leave requests by a specific Team Lead
    List<TeamLeadLeave> findByTeamLeadId(Long teamLeadId);

    // Find all pending leave requests for the Admin to review
    List<TeamLeadLeave> findByStatus(String status);
}