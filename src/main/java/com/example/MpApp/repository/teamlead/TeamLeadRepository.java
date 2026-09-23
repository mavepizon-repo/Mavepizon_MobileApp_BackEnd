package com.example.MpApp.repository.teamlead;

import com.example.MpApp.entity.teamlead.TeamLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeamLeadRepository extends JpaRepository<TeamLead, Long> {

    @Query(value = "select nextval('team_lead_business_id_seq')", nativeQuery = true)
    Long nextBusinessId();

    @Query("select coalesce(avg(t.score), 0) from TeamLead t")
    Double averagePerformanceScore();

    Optional<TeamLead> findByEmail(String email);

    // Get the maximum ID string starting with the branch prefix
    @Query("SELECT MAX(t.teamLeadId) FROM TeamLead t WHERE t.teamLeadId LIKE :prefix%")
    String findMaxTeamLeadIdByPrefix(@Param("prefix") String prefix);
}