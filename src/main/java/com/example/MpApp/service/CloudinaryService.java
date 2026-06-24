package com.example.MpApp.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) return null;

        try {
            String contentType = file.getContentType();
            String resourceType = "auto";

            // Explicit classification for document types
            if (contentType != null && (contentType.contains("pdf") || contentType.contains("msword") || contentType.contains("officedocument"))) {
                resourceType = "raw";
            }

            Map<?, ?> options = ObjectUtils.asMap(
                    "folder", "uploads/" + subFolder,
                    "resource_type", resourceType
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Cloudinary upload crashed: " + e.getMessage(), e);
        }
    }
}