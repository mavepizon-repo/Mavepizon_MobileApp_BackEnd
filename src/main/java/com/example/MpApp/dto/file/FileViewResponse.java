package com.example.MpApp.dto.file;

public class FileViewResponse {
    private String profilePhoto;
    private String aadhaarUrl; // [Aadhaar Redacted]
    private String resumeUrl;

    public FileViewResponse(String profilePhoto, String aadhaarUrl, String resumeUrl) {
        this.profilePhoto = profilePhoto;
        this.aadhaarUrl = aadhaarUrl;
        this.resumeUrl = resumeUrl;
    }

    // --- Getters Only ---
    public String getProfilePhoto() { return profilePhoto; }
    public String getAadhaarUrl() { return aadhaarUrl; }
    public String getResumeUrl() { return resumeUrl; }
}