package com.example.MpApp.dto.telecallerstaff;

public class TelecallingEnquiryRequest {

    private String studentName;

    private String phone;

    private String email;

    private String collegeName;

    private String department;

    private String city;

    private String interestedCourse;

    private String remarks;

    public TelecallingEnquiryRequest() {
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getInterestedCourse() {
        return interestedCourse;
    }

    public void setInterestedCourse(String interestedCourse) {
        this.interestedCourse = interestedCourse;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}