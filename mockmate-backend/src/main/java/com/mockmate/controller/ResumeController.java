package com.mockmate.controller;

import com.mockmate.dto.response.ResumeResponse;
import com.mockmate.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/upload")
    public ResponseEntity<ResumeResponse> uploadResume(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(resumeService.uploadResume(userEmail, file));
    }

    @GetMapping("/me")
    public ResponseEntity<ResumeResponse> getMyResume(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(resumeService.getMyResume(userEmail));
    }
}
