package com.example.MpApp.dto.course;

public class CourseStaffAssignmentRequest {

    /*
    ==========================================================
    ONLINE
    ==========================================================
    */

    private Long onlineStaffId;

    private String onlineZoomLink;


    /*
    ==========================================================
    TISAIYANVILAI
    ==========================================================
    */

    private Long tisaiyanvilaiStaffId;

    private String tisaiyanvilaiZoomLink;


    /*
    ==========================================================
    TIRUNELVELI
    ==========================================================
    */

    private Long tirunelveliStaffId;

    private String tirunelveliZoomLink;


    /*
    ==========================================================
    CONSTRUCTOR
    ==========================================================
    */

    public CourseStaffAssignmentRequest() {
    }


    /*
    ==========================================================
    ONLINE STAFF
    ==========================================================
    */

    public Long getOnlineStaffId() {
        return onlineStaffId;
    }

    public void setOnlineStaffId(Long onlineStaffId) {
        this.onlineStaffId = onlineStaffId;
    }


    public String getOnlineZoomLink() {
        return onlineZoomLink;
    }

    public void setOnlineZoomLink(String onlineZoomLink) {
        this.onlineZoomLink = onlineZoomLink;
    }


    /*
    ==========================================================
    TISAIYANVILAI STAFF
    ==========================================================
    */

    public Long getTisaiyanvilaiStaffId() {
        return tisaiyanvilaiStaffId;
    }

    public void setTisaiyanvilaiStaffId(Long tisaiyanvilaiStaffId) {
        this.tisaiyanvilaiStaffId =
                tisaiyanvilaiStaffId;
    }


    public String getTisaiyanvilaiZoomLink() {
        return tisaiyanvilaiZoomLink;
    }

    public void setTisaiyanvilaiZoomLink(
            String tisaiyanvilaiZoomLink) {

        this.tisaiyanvilaiZoomLink =
                tisaiyanvilaiZoomLink;
    }


    /*
    ==========================================================
    TIRUNELVELI STAFF
    ==========================================================
    */

    public Long getTirunelveliStaffId() {
        return tirunelveliStaffId;
    }

    public void setTirunelveliStaffId(
            Long tirunelveliStaffId) {

        this.tirunelveliStaffId =
                tirunelveliStaffId;
    }


    public String getTirunelveliZoomLink() {
        return tirunelveliZoomLink;
    }

    public void setTirunelveliZoomLink(
            String tirunelveliZoomLink) {

        this.tirunelveliZoomLink =
                tirunelveliZoomLink;
    }
}