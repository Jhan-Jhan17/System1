package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageStorageService {

    // Saves images to a folder named 'uploads' in your project directory
    private final Path rootLocation = Paths.get("uploads");

    public String saveImage(MultipartFile file) {
        try {
            if (file.isEmpty()) return "default.png";
            
            // Create folder if it doesn't exist
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }

            // Generate a unique filename so images don't overwrite each other
            String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), this.rootLocation.resolve(uniqueFilename));
            
            return uniqueFilename;
        } catch (Exception e) {
            throw new RuntimeException("Failed to store image.", e);
        }
    }
}