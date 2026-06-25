package com.example.MpApp.service.course;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.entity.course.Course;
import com.example.MpApp.entity.course.OfferedCourse;
import com.example.MpApp.repository.course.CourseRepository;
import com.example.MpApp.repository.course.OfferedCourseRepository;
import com.example.MpApp.repository.course.StudentCourseRegistrationRepository;
import com.example.MpApp.dto.course.StudentCourseRegistrationRequest;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.entity.course.StudentCourseRegistration;
import com.example.MpApp.repository.student.StudentRepository;
import com.example.MpApp.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentCourseRegistrationService {

    @Autowired
    private StudentCourseRegistrationRepository repository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private OfferedCourseRepository offeredCourseRepository;

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private JwtService jwtService;

    /*
     ==================================
     REGISTER COURSE (BATCH) - UPDATED RESPONSE
     ==================================
     */
    @Transactional
    public Map<String, String> registerCourse(String token, StudentCourseRegistrationRequest request) {
        String email = jwtService.extractEmail(token);
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student Not Found"));

        StudentCourseRegistration registration = new StudentCourseRegistration();
        registration.setStudent(student);

        // 1. Handle Selection Logic (Course OR OfferedCourse)
        if (request.getOfferedCourseId() != null) {
            OfferedCourse batch = offeredCourseRepository.findById(request.getOfferedCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Batch Not Found"));

            // Duplicate check
            if (repository.existsByStudentStudentIdAndOfferedCourseId(student.getStudentId(), batch.getId())) {
                throw new IllegalArgumentException("Already Registered For This Batch");
            }

            // Seat Management
            processSeatManagement(batch, request.getMode());
            offeredCourseRepository.save(batch);
            registration.setOfferedCourse(batch);

        } else if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course Not Found"));

            registration.setCourse(course);
        } else {
            throw new IllegalArgumentException("Please provide either an OfferedCourseId or a CourseId");
        }

        // 2. Set common fields
        registration.setGender(request.getGender());
        registration.setDob(request.getDob());
        registration.setYear(request.getYear());
        registration.setAddress(request.getAddress());
        registration.setProfileImage(request.getProfileImage());
        registration.setMode(request.getMode());
        registration.setPaymentFor(request.getPaymentFor());
        // Get the current count from the repository
        Integer count = repository.countByStudentStudentId(student.getStudentId());

// Standard Java way to handle null
        int newCount = (count == 0) ? 1 : count + 1;

        registration.setRegisteredCoursesCount(newCount);

        StudentCourseRegistration saved = repository.save(registration);

        // 3. Response Construction
        Map<String, String> response = new HashMap<>();
        response.put("registrationId", saved.getId().toString());
        response.put("message", "Registration Successful");
        return response;
    }

    // Helper method to keep main method clean
    private void processSeatManagement(OfferedCourse batch, String mode) {
        if ("ONLINE".equalsIgnoreCase(mode)) {
            if (batch.getOnlineRemainingSeats() <= 0) throw new IllegalStateException("No Online Seats Available");
            batch.setOnlineRemainingSeats(batch.getOnlineRemainingSeats() - 1);
            batch.setOnlineFilledSeats(batch.getOnlineFilledSeats() + 1);
        } else if ("OFFLINE".equalsIgnoreCase(mode)) {
            if (batch.getOfflineRemainingSeats() <= 0) throw new IllegalStateException("No Offline Seats Available");
            batch.setOfflineRemainingSeats(batch.getOfflineRemainingSeats() - 1);
            batch.setOfflineFilledSeats(batch.getOfflineFilledSeats() + 1);
        } else {
            throw new IllegalArgumentException("Invalid Mode. Must be ONLINE or OFFLINE.");
        }
    }

    /*
     ==================================
     GET ALL (UNTOUCHED)
     ==================================
     */
    public List<StudentCourseRegistration> getAllRegistrations() {
        return repository.findAll();
    }

    /*
     ==================================
     GET BY ID (UNTOUCHED)
     ==================================
     */
    public StudentCourseRegistration getRegistrationById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration Not Found"));
    }

    /*
     ==================================
     GET BY STUDENT ID (UNTOUCHED)
     ==================================
     */
    public List<StudentCourseRegistration> getByStudentId(String studentId) {
        return repository.findByStudentStudentId(studentId);
    }

    /*
     ==================================
     DELETE (UNTOUCHED)
     ==================================
     */
    @Transactional
    public void deleteRegistration(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Registration Not Found");
        }
        repository.deleteById(id);
    }

    /*
     ==================================
     GET MY REGISTRATIONS (UNTOUCHED)
     ==================================
     */
    public List<StudentCourseRegistration> getMyRegistrations(String token) {
        String email = jwtService.extractEmail(token);
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student Not Found"));

        return repository.findByStudentStudentId(student.getStudentId());
    }
}