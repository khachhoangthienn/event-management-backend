package com.daniel.eventManagement.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Transactional
    public String uploadImage(MultipartFile file) throws IOException {
        Map image = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", "image"));
        return (String) image.get("secure_url");
    }


    @Transactional
    public List<String> uploadImages(List<MultipartFile> files) throws IOException {
        return files.stream()
                .map(file -> {
                    try {
                        Map image = cloudinary.uploader().upload(file.getBytes(),
                                ObjectUtils.asMap("resource_type", "image"));
                        return (String) image.get("secure_url");

                    } catch (IOException e) {
                        throw new RuntimeException("Error uploading file: " + file.getOriginalFilename(), e);
                    }
                }).toList();
    }

    @Transactional
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "resource_type", "image"));
        } catch (Exception e) {
            throw new RuntimeException("Error deleting file: {}" + publicId, e);
        }
    }

}
