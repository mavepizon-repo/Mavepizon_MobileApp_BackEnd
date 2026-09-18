package com.example.MpApp.dto.course;

public class StudentCourseRegistrationRequest {

    /*
    ==================================
    COURSE
    ==================================

    Course selected by the student
    */

    private Long courseId;


    /*
    ==================================
    MODE
    ==================================

    ONLINE
    OFFLINE
    */

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