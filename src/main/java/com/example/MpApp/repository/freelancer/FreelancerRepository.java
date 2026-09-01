package com.example.MpApp.repository.freelancer;

import com.example.MpApp.entity.freelancer.Freelancer;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FreelancerRepository extends JpaRepository<Freelancer, Long> {

    List<Freelancer> findByDistrictIgnoreCase(String district);

    @Query("SELECT DISTINCT f FROM Freelancer f JOIN f.techStacks t WHERE LOWER(t.name) = LOWER(:techName)")
    List<Freelancer> findByTechStackName(@Param("techName") String techName);

    Optional<Freelancer> findByEmail(String email);
}