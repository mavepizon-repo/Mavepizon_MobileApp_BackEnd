package com.example.MpApp.repository.teamlead;

import com.example.MpApp.entity.teamlead.TeamLeadAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TeamLeadAttendanceRepository extends JpaRepository<TeamLeadAttendance, Long> {
    Optional<TeamLeadAttendance> findByTeamLeadIdAndAttendanceDate(Long teamLeadId, LocalDate date);
    List<TeamLeadAttendance> findByTeamLeadId(Long teamLeadId);
}