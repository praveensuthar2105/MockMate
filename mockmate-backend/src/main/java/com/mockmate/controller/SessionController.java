package com.mockmate.controller;
 
import com.mockmate.dto.request.InterviewRequest;
import com.mockmate.dto.response.InterviewResponse;
import com.mockmate.model.User;
import com.mockmate.repository.UserRepository;
import com.mockmate.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {
 
    private final InterviewService interviewService;
    private final UserRepository userRepository;
    private final com.mockmate.repository.InterviewSessionRepository sessionRepository;
 
    @GetMapping("")
    public ResponseEntity<Page<InterviewResponse>> getSessions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(interviewService.getUserSessionsPaginated(user.getId(), PageRequest.of(page, size)));
    }
 
    @PostMapping("/create")
    public ResponseEntity<InterviewResponse> createSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InterviewRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(interviewService.createSession(user.getId(), request));
    }
 
    @PostMapping("/{id}/start")
    public ResponseEntity<InterviewResponse> startSession(@PathVariable Long id, Authentication authentication) {
        validateSessionOwnership(id, authentication.getName());
        return ResponseEntity.ok(interviewService.startSession(id));
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<InterviewResponse> getSession(@PathVariable Long id, Authentication authentication) {
        validateSessionOwnership(id, authentication.getName());
        return ResponseEntity.ok(interviewService.getSession(id));
    }
 
    @GetMapping("/{id}/report")
    public ResponseEntity<InterviewResponse> getReport(@PathVariable Long id, Authentication authentication) {
        validateSessionOwnership(id, authentication.getName());
        return ResponseEntity.ok(interviewService.getSession(id));
    }
 
    @GetMapping("/{id}/report/pdf")
    public ResponseEntity<byte[]> getPdfReport(@PathVariable Long id, Authentication authentication) {
        validateSessionOwnership(id, authentication.getName());
        byte[] pdfBytes = interviewService.generatePdfReport(id);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"report-" + id + ".pdf\"")
                .body(pdfBytes);
    }
 
    @GetMapping("/me")
    public ResponseEntity<List<InterviewResponse>> getMySessions(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(interviewService.getUserSessions(user.getId()));
    }
 
    @PostMapping("/{id}/phase")
    public ResponseEntity<InterviewResponse> advancePhase(@PathVariable Long id, Authentication authentication) {
        validateSessionOwnership(id, authentication.getName());
        interviewService.advancePhase(id);
        return ResponseEntity.ok(interviewService.getSession(id));
    }
 
    @PostMapping("/{id}/end")
    public ResponseEntity<InterviewResponse> endSession(@PathVariable Long id, Authentication authentication) {
        validateSessionOwnership(id, authentication.getName());
        return ResponseEntity.ok(interviewService.endSession(id));
    }
 
    private void validateSessionOwnership(Long sessionId, String email) {
        com.mockmate.model.InterviewSession session = sessionRepository.findByIdWithUser(sessionId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Session not found"));
        if (!session.getUser().getEmail().equals(email)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Unauthorized access to session");
        }
    }
}
