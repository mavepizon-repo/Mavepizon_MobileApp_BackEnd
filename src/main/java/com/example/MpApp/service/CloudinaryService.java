package com.example.MpApp.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

@Service
public class CloudinaryService {
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final int MAX_IMAGE_DIMENSION = 10000;

    @Autowired private Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) return null;
        validate(file);
        try {
            byte[] bytes = file.getBytes();
            String ext = extension(file.getOriginalFilename());
            String resourceType = isImage(ext) ? "image" : "raw";
            Map<?, ?> options = ObjectUtils.asMap(
                    "folder", "uploads/" + safeFolder(subFolder),
                    "resource_type", resourceType,
                    "use_filename", false,
                    "unique_filename", true,
                    "overwrite", false
            );
            Object secureUrl = cloudinary.uploader().upload(bytes, options).get("secure_url");
            if (secureUrl == null) throw new IllegalStateException("Cloudinary did not return a secure URL");
            return secureUrl.toString();
        } catch (IOException e) {
            throw new RuntimeException("Invalid or unreadable upload", e);
        }
    }

    private void validate(MultipartFile file) {
        if (file.getSize() > MAX_BYTES) throw new IllegalArgumentException("File exceeds 5MB limit");
        String name = file.getOriginalFilename();
        String ext = extension(name);
        if (!ext.matches("png|jpg|jpeg|pdf|doc|docx|xls|xlsx")) throw new IllegalArgumentException("Unsupported file extension");
        try {
            byte[] b = file.getBytes();
            if (isImage(ext)) {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(b));
                if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0 || image.getWidth() > MAX_IMAGE_DIMENSION || image.getHeight() > MAX_IMAGE_DIMENSION)
                    throw new IllegalArgumentException("Invalid image or dimensions exceed limit");
                if ((ext.equals("jpg") || ext.equals("jpeg")) && !(b.length >= 3 && (b[0]&255)==0xFF && (b[1]&255)==0xD8 && (b[2]&255)==0xFF)) throw new IllegalArgumentException("Invalid JPEG signature");
                if (ext.equals("png") && !(b.length >= 8 && (b[0]&255)==0x89 && b[1]==0x50 && b[2]==0x4E && b[3]==0x47)) throw new IllegalArgumentException("Invalid PNG signature");
            } else if (ext.equals("pdf")) {
                String header = new String(b, 0, Math.min(b.length, 5), StandardCharsets.US_ASCII);
                if (!header.startsWith("%PDF-")) throw new IllegalArgumentException("Invalid PDF signature");
            } else {
                if (!(b.length >= 4 && b[0]==0x50 && b[1]==0x4B)) throw new IllegalArgumentException("Invalid Office document signature");
            }
        } catch (IOException e) { throw new IllegalArgumentException("Unable to validate file", e); }
    }
    private boolean isImage(String e){ return e.equals("png")||e.equals("jpg")||e.equals("jpeg"); }
    private String extension(String name){ if(name==null||name.contains("..")||name.contains("/")||name.contains("\\")) throw new IllegalArgumentException("Unsafe filename"); int i=name.lastIndexOf('.'); return i<0?"":name.substring(i+1).toLowerCase(Locale.ROOT); }
    private String safeFolder(String s){ if(s==null || !s.matches("[A-Za-z0-9/_-]+")) throw new IllegalArgumentException("Invalid upload folder"); return s; }
}
