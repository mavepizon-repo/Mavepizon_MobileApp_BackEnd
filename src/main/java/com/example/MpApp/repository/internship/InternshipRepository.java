package com.example.MpApp.repository.internship;

import com.example.MpApp.entity.internship.Internship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {
    Optional<Internship> findByInternshipCode(String internshipCode);

    // Counts records matching current tracking prefix to increment Mid_no safely
    @Query("SELECT COUNT(i) FROM Internship i WHERE i.internshipCode LIKE :prefix%")
    long countByInternshipCodePrefix(@Param("prefix") String prefix);
}