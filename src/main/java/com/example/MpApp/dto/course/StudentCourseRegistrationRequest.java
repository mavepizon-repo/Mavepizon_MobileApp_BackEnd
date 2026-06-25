package com.example.MpApp.dto.course;

import java.time.LocalDate;

public class StudentCourseRegistrationRequest {

    private Long offeredCourseId;

    private Long courseId;
// Update getters/setters

    private String gender;

    private LocalDate dob;

    private String year;

    private String address;

    private String profileImage;

    private String mode; // ONLINE / OFFLINE

    private String paymentFor;

    public StudentCourseRegistrationRequest() {
    }

    public Long getOfferedCourseId() {
        return offeredCourseId;
    }

    public void setOfferedCourseId(Long offeredCourseId) {
        this.offeredCourseId = offeredCourseId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getPaymentFor() {
        return paymentFor;
    }

    public void setPaymentFor(String paymentFor) {
        this.paymentFor = paymentFor;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
}