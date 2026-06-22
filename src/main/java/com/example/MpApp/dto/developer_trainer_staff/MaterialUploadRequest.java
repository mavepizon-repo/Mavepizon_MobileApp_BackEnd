package com.example.MpApp.dto.developer_trainer_staff;

public class MaterialUploadRequest {

    private Long batchId;

    private String title;

    private String fileUrl;

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Long getBatchId() {
        return batchId;
    }
}