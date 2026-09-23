package com.example.MpApp.repository.student;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.MpApp.entity.student.Student;

public interface StudentRepository
        extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(
            String email);
    Optional<Student> findByStudentId(String studentId);

    Page<Student> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrStudentIdContainingIgnoreCase(
            String name, String email, String studentId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "select nextval('student_business_id_seq')", nativeQuery = true)
    Long nextBusinessId();
    boolean existsByEmail(
            String email);

    boolean existsByMobileNumber(
            String phone);
}