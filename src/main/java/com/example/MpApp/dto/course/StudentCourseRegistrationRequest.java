package com.example.MpApp.dto.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class StudentCourseRegistrationRequest {

    /*
    ==================================
    COURSE
    ==================================

    Course selected by the student
    */

    @NotNull(message = "Course ID is required")
    @jakarta.validation.constraints.Positive(message = "Course ID must be positive")
    private Long courseId;


    /*
    ==================================
    MODE
    ==================================

    ONLINE
    OFFLINE
    */

    @NotBlank(message = "Mode is required")
    @Pattern(regexp = "ONLINE|OFFLINE", message = "Mode must be ONLINE or OFFLINE")
    private String mode;


    /*
    ==================================
    LOCATION
    ==================================

    ONLINE
        -> null

    OFFLINE
        -> TIRUNELVELI
        -> TISAIYANVILAI
    */

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;


    /*
    ==================================
    CONSTRUCTOR
    ==================================
    */

    public StudentCourseRegistrationRequest() {
    }


    /*
    ==================================
    GETTERS & SETTERS
    ==================================
    */

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }


    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}