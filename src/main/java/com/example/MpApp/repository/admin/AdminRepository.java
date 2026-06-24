package com.example.MpApp.repository.admin;

import com.example.MpApp.entity.admin.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin,Long> {
    Optional<Admin> findByEmail(String email);

    boolean existsByEmail(String email);
}
