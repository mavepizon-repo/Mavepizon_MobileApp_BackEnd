package com.example.MpApp.service.course;

import com.example.MpApp.dto.course.CourseStaffAssignmentRequest;
import com.example.MpApp.entity.course.Course;
import com.example.MpApp.entity.course.CourseStaffAssignment;
import com.example.MpApp.repository.course.CourseRepository;
import com.example.MpApp.repository.course.CourseStaffAssignmentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseStaffAssignmentService {

    @Autowired
    private CourseStaffAssignmentRepository assignmentRepository;

    @Autowired
    private CourseRepository courseRepository;


    // =========================================================
    // CREATE ASSIGNMENT
    // =========================================================

    @Transactional
    public CourseStaffAssignment createAssignment(
            Long courseId,
            CourseStaffAssignmentRequest request,
            String assignedBy) {

        // -----------------------------------------------------
        // FIND COURSE
        // -----------------------------------------------------

        Course course =
                courseRepository.findById(courseId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course not found with ID: "
                                                + courseId
                                )
                        );


        // -----------------------------------------------------
        // PREVENT DUPLICATE ASSIGNMENT
        // -----------------------------------------------------

        if (assignmentRepository.existsByCourseId(courseId)) {

            throw new RuntimeException(
                    "Staff assignment already exists for this course"
            );
        }


        // -----------------------------------------------------
        // VALIDATE AT LEAST ONE STAFF
        // -----------------------------------------------------

        if (request.getOnlineStaffId() == null &&
                request.getTisaiyanvilaiStaffId() == null &&
                request.getTirunelveliStaffId() == null) {

            throw new RuntimeException(
                    "At least one staff member must be assigned"
            );
        }


        // -----------------------------------------------------
        // CREATE ASSIGNMENT
        // -----------------------------------------------------

        CourseStaffAssignment assignment =
                new CourseStaffAssignment();

        assignment.setCourse(course);


        // =====================================================
        // ONLINE
        // =====================================================

        assignment.setOnlineStaffId(
                request.getOnlineStaffId()
        );

        assignment.setOnlineZoomLink(
                request.getOnlineZoomLink()
        );


        // =====================================================
        // TISAIYANVILAI
        // =====================================================

        assignment.setTisaiyanvilaiStaffId(
                request.getTisaiyanvilaiStaffId()
        );

        assignment.setTisaiyanvilaiZoomLink(
                request.getTisaiyanvilaiZoomLink()
        );


        // =====================================================
        // TIRUNELVELI
        // =====================================================

        assignment.setTirunelveliStaffId(
                request.getTirunelveliStaffId()
        );

        assignment.setTirunelveliZoomLink(
                request.getTirunelveliZoomLink()
        );


        // =====================================================
        // AUDIT
        // =====================================================

        assignment.setAssignedBy(assignedBy);


        return assignmentRepository.save(assignment);
    }


    // =========================================================
    // UPDATE ASSIGNMENT
    // =========================================================

    @Transactional
    public CourseStaffAssignment updateAssignment(
            Long courseId,
            CourseStaffAssignmentRequest request,
            String updatedBy) {

        CourseStaffAssignment assignment =
                assignmentRepository
                        .findByCourseId(courseId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Staff assignment not found for course ID: "
                                                + courseId
                                )
                        );


        // -----------------------------------------------------
        // VALIDATE AT LEAST ONE STAFF
        // -----------------------------------------------------

        if (request.getOnlineStaffId() == null &&
                request.getTisaiyanvilaiStaffId() == null &&
                request.getTirunelveliStaffId() == null) {

            throw new RuntimeException(
                    "At least one staff member must be assigned"
            );
        }


        // =====================================================
        // UPDATE ONLINE
        // =====================================================

        assignment.setOnlineStaffId(
                request.getOnlineStaffId()
        );

        assignment.setOnlineZoomLink(
                request.getOnlineZoomLink()
        );


        // =====================================================
        // UPDATE TISAIYANVILAI
        // =====================================================

        assignment.setTisaiyanvilaiStaffId(
                request.getTisaiyanvilaiStaffId()
        );

        assignment.setTisaiyanvilaiZoomLink(
                request.getTisaiyanvilaiZoomLink()
        );


        // =====================================================
        // UPDATE TIRUNELVELI
        // =====================================================

        assignment.setTirunelveliStaffId(
                request.getTirunelveliStaffId()
        );

        assignment.setTirunelveliZoomLink(
                request.getTirunelveliZoomLink()
        );


        // =====================================================
        // AUDIT
        // =====================================================

        assignment.setAssignedBy(updatedBy);


        return assignmentRepository.save(assignment);
    }


    // =========================================================
    // GET ASSIGNMENT BY COURSE
    // =========================================================

    @Transactional(readOnly = true)
    public CourseStaffAssignment getAssignmentByCourse(
            Long courseId) {

        return assignmentRepository
                .findByCourseId(courseId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Staff assignment not found for course ID: "
                                        + courseId
                        )
                );
    }


    // =========================================================
    // GET ALL ASSIGNMENTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<CourseStaffAssignment> getAllAssignments() {

        return assignmentRepository.findAll();
    }


    // =========================================================
    // DELETE ASSIGNMENT
    // =========================================================

    @Transactional
    public void deleteAssignment(Long courseId) {

        if (!assignmentRepository.existsByCourseId(courseId)) {

            throw new RuntimeException(
                    "Staff assignment not found for course ID: "
                            + courseId
            );
        }

        assignmentRepository.deleteByCourseId(courseId);
    }
}