package com.example.MpApp.repository.teamlead;

import com.example.MpApp.entity.teamlead.TeamLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface TeamLeadRepository extends JpaRepository<TeamLead, Long> {
    Optional<TeamLead> findByEmail(String email);

    // Get the maximum ID string starting with the branch prefix
    @Query("SELECT MAX(t.teamLeadId) FROM TeamLead t WHERE t.teamLeadId LIKE :prefix%")
    String findMaxTeamLeadIdByPrefix(@Param("prefix") String prefix);
}