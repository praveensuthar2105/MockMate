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
        return ResponseEntity.ok(interviewService.createSession(user.getId(), request));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<InterviewResponse> startSession(@PathVariable Long id) {
        return ResponseEntity.ok(interviewService.startSession(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterviewResponse> getSession(@PathVariable Long id) {
        return ResponseEntity.ok(interviewService.getSession(id));
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<InterviewResponse> getReport(@PathVariable Long id) {
        return ResponseEntity.ok(interviewService.getSession(id));
    }

    @GetMapping("/me")
    public ResponseEntity<List<InterviewResponse>> getMySessions(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(interviewService.getUserSessions(user.getId()));
    }

    @PostMapping("/{id}/phase")
    public ResponseEntity<InterviewResponse> advancePhase(@PathVariable Long id) {
        interviewService.advancePhase(id);
        return ResponseEntity.ok(interviewService.getSession(id));
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<InterviewResponse> endSession(@PathVariable Long id) {
        return ResponseEntity.ok(interviewService.endSession(id));
    }
}
