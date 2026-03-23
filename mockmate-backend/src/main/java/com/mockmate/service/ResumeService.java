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
    private final GeminiParsingService geminiParsingService;

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

            // Use Gemini to parse resume text into structured JSON
            String parsedJson = "{}";
            try {
                parsedJson = geminiParsingService.parseResume(rawText);
            } catch (Exception e) {
                // Fallback handled inside GeminiParsingService
                parsedJson = "{\"status\": \"failed\"}";
            }

            // Extract basic info from the JSON if needed or just store the JSON
            String skills = extractFromJson(parsedJson, "skills");
            String summary = extractFromJson(parsedJson, "summary");

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

    private String extractFromJson(String json, String key) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json);
            if (root.has(key)) {
                return root.get(key).toString();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "Not specified";
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
