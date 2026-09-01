package com.example.MpApp.repository.freelancer;


import com.example.MpApp.entity.freelancer.TechStack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TechStackRepository extends JpaRepository<TechStack, Long> {
    Optional<TechStack> findByNameIgnoreCase(String name);
}