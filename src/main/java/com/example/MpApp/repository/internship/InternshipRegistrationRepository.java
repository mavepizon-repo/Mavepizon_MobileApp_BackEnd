package com.example.MpApp.repository.internship;

import com.example.MpApp.entity.internship.InternshipRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InternshipRegistrationRepository extends JpaRepository<InternshipRegistration, Long> {
}