package com.example.MpApp.dto.developer_trainer_staff;

public class ZoomLinkRequest {

    private String zoomLink;

    private Long batchId;

    public String getZoomLink() {
        return zoomLink;
    }

    public void setZoomLink(String zoomLink) {
        this.zoomLink = zoomLink;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batch_id) {
        this.batchId = batch_id;
    }
}