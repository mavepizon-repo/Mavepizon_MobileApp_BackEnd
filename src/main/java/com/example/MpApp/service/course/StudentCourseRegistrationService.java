package com.example.MpApp.service.course;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.entity.course.OfferedCourse;
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

        // 1. Fetch the specific batch (OfferedCourse)
        OfferedCourse batch = offeredCourseRepository.findById(request.getOfferedCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch Not Found"));

        // 2. Check for duplicate registration in THIS specific batch
        boolean alreadyRegistered = repository.existsByStudentStudentIdAndOfferedCourseId(
                student.getStudentId(), batch.getId());

        if (alreadyRegistered) {
            throw new IllegalArgumentException("Already Registered For This Batch");
        }

        // 3. Seat Management on the OfferedCourse
        if ("ONLINE".equalsIgnoreCase(request.getMode())) {
            if (batch.getOnlineRemainingSeats() <= 0) {
                throw new IllegalStateException("No Online Seats Available in this Batch");
            }
            batch.setOnlineRemainingSeats(batch.getOnlineRemainingSeats() - 1);
            batch.setOnlineFilledSeats(batch.getOnlineFilledSeats() + 1);

        } else if ("OFFLINE".equalsIgnoreCase(request.getMode())) {
            if (batch.getOfflineRemainingSeats() <= 0) {
                throw new IllegalStateException("No Offline Seats Available in this Batch");
            }
            batch.setOfflineRemainingSeats(batch.getOfflineRemainingSeats() - 1);
            batch.setOfflineFilledSeats(batch.getOfflineFilledSeats() + 1);

        } else {
            throw new IllegalArgumentException("Invalid Mode. Must be ONLINE or OFFLINE.");
        }

        offeredCourseRepository.save(batch);

        // 4. Update Registration Count
        Integer count = repository.countByStudentStudentId(student.getStudentId());
        int newCount = (count == null) ? 1 : count + 1;

        // 5. Create Registration Record
        StudentCourseRegistration registration = new StudentCourseRegistration();
        registration.setStudent(student);
        registration.setOfferedCourse(batch); // Linked to the Batch!
        registration.setGender(request.getGender());
        registration.setDob(request.getDob());
        registration.setYear(request.getYear());
        registration.setAddress(request.getAddress());
        registration.setProfileImage(request.getProfileImage());
        registration.setMode(request.getMode());
        registration.setPaymentFor(request.getPaymentFor());
        registration.setRegisteredCoursesCount(newCount);

        StudentCourseRegistration savedRegistration = repository.save(registration);

        // Building the refined, minimal key payload map
        Map<String, String> response = new HashMap<>();
        response.put("registrationId", savedRegistration.getId().toString());
        response.put("offeredCourseId", batch.getId().toString());
        response.put("message", "Course Batch Registered Successfully");
        return response;
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