package com.example.MpApp.repository.teamlead;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.MpApp.entity.teamlead.TeamLead;

public interface TeamLeadRepository
        extends JpaRepository<TeamLead, Long> {

    Optional<TeamLead> findByEmail(
            String email);

    long countByTeamLeadIdStartingWith(
            String prefix);
}