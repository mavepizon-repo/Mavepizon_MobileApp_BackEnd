package com.example.MpApp.service.developer_trainer;

import com.example.MpApp.dto.developer_trainer_staff.AttendanceRequest;
import com.example.MpApp.entity.course.Course;
import com.example.MpApp.entity.course.CourseStaffAssignment;
import com.example.MpApp.entity.course.StudentCourseRegistration;
import com.example.MpApp.entity.developer_trainer_staff.CourseMaterial;
import com.example.MpApp.entity.developer_trainer_staff.StudentAttendance;
import com.example.MpApp.entity.enums.StaffCategory;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.student.Student;

import com.example.MpApp.exception.ResourceNotFoundException;

import com.example.MpApp.repository.course.CourseRepository;
import com.example.MpApp.repository.course.CourseStaffAssignmentRepository;
import com.example.MpApp.repository.course.StudentCourseRegistrationRepository;

import com.example.MpApp.repository.developer_trainer.CourseMaterialRepository;
import com.example.MpApp.repository.developer_trainer.StudentAttendanceRepository;

import com.example.MpApp.repository.officestaff.OfficeStaffRepository;
import com.example.MpApp.repository.student.StudentRepository;

import com.example.MpApp.service.CloudinaryService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeveloperTrainerService {

    private final OfficeStaffRepository officeStaffRepository;

    private final CourseRepository courseRepository;

    private final CourseStaffAssignmentRepository
            courseStaffAssignmentRepository;

    private final StudentRepository studentRepository;

    private final StudentCourseRegistrationRepository
            studentCourseRegistrationRepository;

    private final StudentAttendanceRepository attendanceRepository;

    private final CourseMaterialRepository materialRepository;

    private final CloudinaryService cloudinaryService;


    @Value("${file.upload-dir.course-materials:uploads/materials/}")
    private String uploadDir;


    // =========================================================
    // VALIDATE DEVELOPER + TRAINER STAFF
    // =========================================================

    private OfficeStaff validateTrainer(Long staffId) {

        OfficeStaff staff =
                officeStaffRepository.findById(staffId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Developer Trainer staff not found for ID: "
                                                + staffId
                                )
                        );


        /*
         * Only Developer + Trainer staff are allowed
         * to access this module.
         */

        if (staff.getCategory() != StaffCategory.DEVELOPER_TRAINER
                && !"TRAINER".equalsIgnoreCase(
                staff.getRole()
        )) {

            throw new IllegalArgumentException(
                    "Only Developer Trainer staff can access this module"
            );
        }

        return staff;
    }


    // =========================================================
    // VALIDATE STAFF ASSIGNMENT TO COURSE
    // =========================================================

    private CourseStaffAssignment
    validateStaffAssignment(
            Long staffId,
            Long courseId) {

        CourseStaffAssignment assignment =
                courseStaffAssignmentRepository
                        .findByCourseId(courseId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Staff assignment not found for course ID: "
                                                + courseId
                                )
                        );


        /*
         * Check whether this staff member is assigned
         * to this course.
         */

        boolean assigned =
                staffId.equals(
                        assignment.getOnlineStaffId()
                )
                        ||
                        staffId.equals(
                                assignment.getTisaiyanvilaiStaffId()
                        )
                        ||
                        staffId.equals(
                                assignment.getTirunelveliStaffId()
                        );


        if (!assigned) {

            throw new IllegalArgumentException(
                    "Access Denied: You are not assigned to this course"
            );
        }


        return assignment;
    }


    // =========================================================
    // ATTENDANCE
    // =========================================================

    @Transactional
    public Map<String, String> markAttendance(
            Long staffId,
            AttendanceRequest request) {

        // -----------------------------------------------------
        // VALIDATE STAFF
        // -----------------------------------------------------

        validateTrainer(staffId);


        // -----------------------------------------------------
        // VALIDATE COURSE
        // -----------------------------------------------------

        Long courseId =
                request.getCourseId();

        Course course =
                courseRepository.findById(courseId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Course not found for ID: "
                                                + courseId
                                )
                        );


        // -----------------------------------------------------
        // VALIDATE STAFF ASSIGNMENT
        // -----------------------------------------------------

        validateStaffAssignment(
                staffId,
                courseId
        );


        // -----------------------------------------------------
        // ATTENDANCE DATE
        // -----------------------------------------------------

        LocalDate attendanceDate =
                request.getDate() != null
                        ? request.getDate()
                        : LocalDate.now();


        // -----------------------------------------------------
        // GET STUDENTS REGISTERED FOR COURSE
        // -----------------------------------------------------

        List<StudentCourseRegistration> registrations =
                studentCourseRegistrationRepository
                        .findByCourseId(courseId);


        Set<String> registeredStudentIds =
                registrations.stream()
                        .map(registration ->
                                registration
                                        .getStudent()
                                        .getStudentId()
                        )
                        .collect(Collectors.toSet());


        // -----------------------------------------------------
        // CREATE / UPDATE ATTENDANCE
        // -----------------------------------------------------

        List<StudentAttendance> records =
                request.getStudents()
                        .stream()
                        .map(dto -> {

                            Student student =
                                    studentRepository
                                            .findById(
                                                    dto.getStudentId()
                                            )
                                            .orElseThrow(() ->
                                                    new ResourceNotFoundException(
                                                            "Student not found for ID: "
                                                                    + dto.getStudentId()
                                                    )
                                            );


                            /*
                             * Student must be registered
                             * for this course.
                             */

                            if (!registeredStudentIds.contains(
                                    student.getStudentId()
                            )) {

                                throw new IllegalArgumentException(
                                        "Student "
                                                + student.getStudentId()
                                                + " is not registered for this course"
                                );
                            }


                            /*
                             * Check whether attendance already
                             * exists for this student/course/date.
                             */

                            List<StudentAttendance> existingRecords =
                                    attendanceRepository
                                            .findByCourseIdAndAttendanceDate(
                                                    courseId,
                                                    attendanceDate
                                            );


                            StudentAttendance attendance =
                                    existingRecords.stream()
                                            .filter(record ->
                                                    record.getStudent()
                                                            .getId()
                                                            .equals(
                                                                    student.getId()
                                                            )
                                            )
                                            .findFirst()
                                            .orElseGet(
                                                    StudentAttendance::new
                                            );


                            attendance.setCourse(course);

                            attendance.setStudent(student);

                            attendance.setAttendanceDate(
                                    attendanceDate
                            );

                            attendance.setPresent(
                                    dto.isPresent()
                            );

                            attendance.setMarkedByStaffId(
                                    staffId
                            );


                            return attendance;

                        })
                        .collect(Collectors.toList());


        // -----------------------------------------------------
        // SAVE ATTENDANCE
        // -----------------------------------------------------

        attendanceRepository.saveAll(records);


        // -----------------------------------------------------
        // RESPONSE
        // -----------------------------------------------------

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "courseId",
                courseId.toString()
        );

        response.put(
                "attendanceDate",
                attendanceDate.toString()
        );

        response.put(
                "totalRecordsMarked",
                String.valueOf(records.size())
        );

        response.put(
                "message",
                "Student Attendance Saved Successfully"
        );


        return response;
    }


    // =========================================================
    // COURSE MATERIAL UPLOAD
    // =========================================================

    @Transactional
    public Map<String, String> uploadMaterial(
            Long staffId,
            Long courseId,
            String title,
            MultipartFile file) {


        // -----------------------------------------------------
        // VALIDATE STAFF
        // -----------------------------------------------------

        validateTrainer(staffId);


        // -----------------------------------------------------
        // VALIDATE COURSE
        // -----------------------------------------------------

        Course course =
                courseRepository.findById(courseId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Course not found for ID: "
                                                + courseId
                                )
                        );


        // -----------------------------------------------------
        // VALIDATE STAFF ASSIGNMENT
        // -----------------------------------------------------

        validateStaffAssignment(
                staffId,
                courseId
        );


        // -----------------------------------------------------
        // VALIDATE FILE
        // -----------------------------------------------------

        if (file == null ||
                file.isEmpty()) {

            throw new IllegalArgumentException(
                    "Uploaded file cannot be empty"
            );
        }


        // -----------------------------------------------------
        // VALIDATE TITLE
        // -----------------------------------------------------

        if (title == null ||
                title.isBlank()) {

            throw new IllegalArgumentException(
                    "Material title is required"
            );
        }


        // -----------------------------------------------------
        // UPLOAD TO CLOUDINARY
        // -----------------------------------------------------

        String secureUrl =
                cloudinaryService.uploadFile(
                        file,
                        "material"
                );


        // -----------------------------------------------------
        // CREATE COURSE MATERIAL
        // -----------------------------------------------------

        CourseMaterial material =
                new CourseMaterial();

        material.setCourse(course);

        material.setUploadedByStaffId(
                staffId
        );

        material.setTitle(title);

        material.setFileUrl(secureUrl);


        // -----------------------------------------------------
        // SAVE
        // -----------------------------------------------------

        CourseMaterial saved =
                materialRepository.save(
                        material
                );


        // -----------------------------------------------------
        // RESPONSE
        // -----------------------------------------------------

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "materialId",
                saved.getId().toString()
        );

        response.put(
                "courseId",
                courseId.toString()
        );

        response.put(
                "fileUrl",
                secureUrl
        );

        response.put(
                "message",
                "Course Study Material Uploaded Successfully"
        );


        return response;
    }


    // =========================================================
    // GET COURSE MATERIALS
    // =========================================================

    @Transactional(readOnly = true)
    public List<CourseMaterial> getCourseMaterials(
            Long staffId,
            Long courseId) {

        validateTrainer(staffId);

        validateStaffAssignment(
                staffId,
                courseId
        );

        return materialRepository.findByCourseId(
                courseId
        );
    }


    // =========================================================
    // GET COURSE MATERIALS UPLOADED BY STAFF
    // =========================================================

    @Transactional(readOnly = true)
    public List<CourseMaterial> getMyMaterials(
            Long staffId,
            Long courseId) {

        validateTrainer(staffId);

        validateStaffAssignment(
                staffId,
                courseId
        );

        return materialRepository
                .findByCourseIdAndUploadedByStaffId(
                        courseId,
                        staffId
                );
    }


    // =========================================================
    // GET COURSE ATTENDANCE
    // =========================================================

    @Transactional(readOnly = true)
    public List<StudentAttendance> getCourseAttendance(
            Long staffId,
            Long courseId) {

        validateTrainer(staffId);

        validateStaffAssignment(
                staffId,
                courseId
        );

        return attendanceRepository.findByCourseId(
                courseId
        );
    }


    // =========================================================
    // GET ATTENDANCE BY DATE
    // =========================================================

    @Transactional(readOnly = true)
    public List<StudentAttendance> getCourseAttendanceByDate(
            Long staffId,
            Long courseId,
            LocalDate date) {

        validateTrainer(staffId);

        validateStaffAssignment(
                staffId,
                courseId
        );

        return attendanceRepository
                .findByCourseIdAndAttendanceDate(
                        courseId,
                        date
                );
    }
}