package com.example.MpApp.controller.developer_trainer;

import com.example.MpApp.dto.developer_trainer_staff.AttendanceRequest;
import com.example.MpApp.service.developer_trainer.DeveloperTrainerService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/trainer")
@CrossOrigin("*")
@RequiredArgsConstructor
public class DeveloperTrainerController {

    private final DeveloperTrainerService trainerService;


    // =========================================================
    // 1. MARK ATTENDANCE
    // =========================================================
    /*
     * Student attendance is handled course-wise.
     *
     * AttendanceRequest contains:
     *
     * courseId
     * date
     * students[]
     *
     * Staff ID comes from the URL.
     */

    @PostMapping("/{staffId}/attendance")
    public ResponseEntity<?> markAttendance(

            @PathVariable Long staffId,

            @RequestBody AttendanceRequest request) {

        try {

            return ResponseEntity.ok(
                    trainerService.markAttendance(
                            staffId,
                            request
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // 2. UPLOAD COURSE MATERIAL
    // =========================================================
    /*
     * Course ID is taken from the URL.
     *
     * Staff can upload material only if that staff
     * is assigned to the course.
     *
     * Required multipart fields:
     *
     * title
     * file
     */

    @PostMapping(
            value = "/{staffId}/courses/{courseId}/material",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadMaterial(

            @PathVariable Long staffId,

            @PathVariable Long courseId,

            @RequestParam("title")
            String title,

            @RequestParam("file")
            MultipartFile file) {

        try {

            return ResponseEntity.ok(
                    trainerService.uploadMaterial(
                            staffId,
                            courseId,
                            title,
                            file
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // 3. GET COURSE MATERIALS
    // =========================================================
    /*
     * Returns all materials uploaded for a course.
     */

    @GetMapping("/{staffId}/courses/{courseId}/materials")
    public ResponseEntity<?> getCourseMaterials(

            @PathVariable Long staffId,

            @PathVariable Long courseId) {

        try {

            return ResponseEntity.ok(
                    trainerService.getCourseMaterials(
                            staffId,
                            courseId
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // 4. GET MY MATERIALS
    // =========================================================
    /*
     * Returns only materials uploaded by
     * the currently selected staff member
     * for the selected course.
     */

    @GetMapping("/{staffId}/courses/{courseId}/my-materials")
    public ResponseEntity<?> getMyMaterials(

            @PathVariable Long staffId,

            @PathVariable Long courseId) {

        try {

            return ResponseEntity.ok(
                    trainerService.getMyMaterials(
                            staffId,
                            courseId
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // 5. GET COURSE ATTENDANCE
    // =========================================================
    /*
     * Returns all attendance records
     * for the selected course.
     */

    @GetMapping("/{staffId}/courses/{courseId}/attendance")
    public ResponseEntity<?> getCourseAttendance(

            @PathVariable Long staffId,

            @PathVariable Long courseId) {

        try {

            return ResponseEntity.ok(
                    trainerService.getCourseAttendance(
                            staffId,
                            courseId
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // 6. GET COURSE ATTENDANCE BY DATE
    // =========================================================
    /*
     * Returns attendance for one particular date.
     *
     * Example:
     *
     * /api/trainer/5/courses/10/attendance/date?date=2026-08-15
     */

    @GetMapping(
            "/{staffId}/courses/{courseId}/attendance/date"
    )
    public ResponseEntity<?> getCourseAttendanceByDate(

            @PathVariable Long staffId,

            @PathVariable Long courseId,

            @RequestParam LocalDate date) {

        try {

            return ResponseEntity.ok(
                    trainerService.getCourseAttendanceByDate(
                            staffId,
                            courseId,
                            date
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }
}