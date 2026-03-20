package com.mockmate.service;

import com.mockmate.dto.response.ResumeResponse;
import com.mockmate.model.Resume;
import com.mockmate.model.User;
import com.mockmate.repository.ResumeRepository;
import com.mockmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final PdfParserService pdfParserService;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Transactional
    public ResumeResponse uploadResume(String userEmail, MultipartFile file) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".pdf")) {
            throw new IllegalArgumentException("File must be a valid PDF");
        }

        try {
            // Save file
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Extract Text
            String rawText = pdfParserService.extractText(file);

            // For now, simply map raw text to parsed JSON placeholders.
            // In Phase 2, an AI agent will convert rawText into structured JSON/skills.
            String parsedJson = "{\"status\": \"pending\"}";
            String skills = "Raw Upload";
            String summary = "Raw Upload";

            // Save to DB
            Resume resume = resumeRepository.findByUserId(user.getId()).orElse(new Resume());
            resume.setUser(user);
            resume.setFilePath(filePath.toString());
            resume.setOriginalFileName(file.getOriginalFilename());
            resume.setRawText(rawText);
            resume.setParsedJson(parsedJson);
            resume.setSkills(skills);
            resume.setSummary(summary);

            Resume savedResume = resumeRepository.save(resume);

            // Update User profile status
            user.setProfileComplete(true);
            userRepository.save(user);

            return mapToResponse(savedResume);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Transactional(readOnly = true)
    public ResumeResponse getMyResume(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Resume resume = resumeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        return mapToResponse(resume);
    }

    private ResumeResponse mapToResponse(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getOriginalFileName(),
                resume.getParsedJson(),
                resume.getSkills(),
                resume.getSummary(),
                resume.getUploadedAt());
    }
}
