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

    public ResumeResponse uploadResume(String userEmail, MultipartFile file) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (file.isEmpty() || file.getOriginalFilename() == null || !file.getOriginalFilename().endsWith(".pdf")) {
            throw new IllegalArgumentException("File must be a valid PDF");
        }

        try {
            // Save file
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Sanitize filename to prevent path traversal
            String originalFilename = file.getOriginalFilename();
            String sanitizedFilename = new File(originalFilename).getName();
            sanitizedFilename = sanitizedFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            if (sanitizedFilename.isEmpty()) {
                sanitizedFilename = "resume.pdf";
            }

            String uniqueFileName = UUID.randomUUID().toString() + "_" + sanitizedFilename;
            Path filePath = Paths.get(uploadDir, uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Extract Text
            String rawText = pdfParserService.extractText(file);

            // Use Gemini to parse resume text into structured JSON (runs outside active transaction)
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

            // Save to DB and complete profile (runs in a short transaction)
            Resume savedResume = persistResumeAndCompleteProfile(user, filePath.toString(), originalFilename, rawText, parsedJson, skills, summary);

            return mapToResponse(savedResume);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Transactional
    public Resume persistResumeAndCompleteProfile(User user, String filePath, String originalFileName, String rawText, String parsedJson, String skills, String summary) {
        Resume resume = resumeRepository.findByUserId(user.getId()).orElse(new Resume());
        resume.setUser(user);
        resume.setFilePath(filePath);
        resume.setOriginalFileName(originalFileName);
        resume.setRawText(rawText);
        resume.setParsedJson(parsedJson);
        resume.setSkills(skills);
        resume.setSummary(summary);

        Resume savedResume = resumeRepository.save(resume);

        // Update User profile status
        user.setProfileComplete(true);
        userRepository.save(user);

        return savedResume;
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
