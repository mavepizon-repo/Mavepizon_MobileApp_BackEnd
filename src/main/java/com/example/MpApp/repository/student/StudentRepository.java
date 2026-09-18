package com.example.MpApp.repository.student;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.MpApp.entity.student.Student;

public interface StudentRepository
        extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(
            String email);
    Optional<Student> findByStudentId(String studentId);

    boolean existsByEmail(
            String email);

    boolean existsByMobileNumber(
            String phone);
}