package com.mockmate.controller;

import com.mockmate.dto.request.InterviewRequest;
import com.mockmate.dto.response.InterviewResponse;
import com.mockmate.model.PhaseType;
import com.mockmate.model.User;
import com.mockmate.repository.UserRepository;
import com.mockmate.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final UserRepository userRepository;

    @PostMapping("/start")
    public ResponseEntity<InterviewResponse> startInterview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InterviewRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(interviewService.startSession(user.getId(), request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterviewResponse> getInterview(@PathVariable Long id) {
        return ResponseEntity.ok(interviewService.getSession(id));
    }

    @GetMapping("/me")
    public ResponseEntity<List<InterviewResponse>> getMyInterviews(
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
    public ResponseEntity<InterviewResponse> endInterview(@PathVariable Long id) {
        return ResponseEntity.ok(interviewService.endSession(id));
    }
}
