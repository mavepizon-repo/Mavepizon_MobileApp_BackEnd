package com.example.MpApp.entity.student;

import com.example.MpApp.entity.course.StudentCourseRegistration;
import com.example.MpApp.entity.telecallerstaff.TelecallingEnquiry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String studentId;

    private String gender;

    private String collegeName;

    private String department;

    private String mobileNumber;

    @Column(unique = true)
    private String email;

    @JsonIgnore
    private String password;

    private String profilePhoto;

    private String skills;

    /*
     ==================================
     COURSE REGISTRATIONS
     ==================================
     */

    @JsonIgnore
    @OneToMany(
            mappedBy = "student",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private List<StudentCourseRegistration>
            courseRegistrations = new ArrayList<>();


    /*
     ==================================
     TELECALLING ENQUIRIES
     ==================================
     */

    @JsonIgnore
    @OneToMany(
            mappedBy = "student",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private List<TelecallingEnquiry>
            telecallingEnquiries = new ArrayList<>();


    public Student() {
    }

    // ================= ID =================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // ================= BASIC =================

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // ================= SECURITY =================

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // ================= PROFILE =================

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    // ================= COURSE =================

    public List<StudentCourseRegistration> getCourseRegistrations() {
        return courseRegistrations;
    }

    public void setCourseRegistrations(
            List<StudentCourseRegistration> courseRegistrations) {
        this.courseRegistrations = courseRegistrations;
    }

    // ================= TELECALLING =================

    public List<TelecallingEnquiry> getTelecallingEnquiries() {
        return telecallingEnquiries;
    }

    public void setTelecallingEnquiries(
            List<TelecallingEnquiry> telecallingEnquiries) {
        this.telecallingEnquiries = telecallingEnquiries;
    }
}