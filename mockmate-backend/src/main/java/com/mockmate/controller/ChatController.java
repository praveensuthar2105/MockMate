package com.mockmate.controller;

import com.mockmate.dto.request.ChatRequest;
import com.mockmate.dto.response.ChatResponse;
import com.mockmate.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final com.mockmate.repository.InterviewSessionRepository sessionRepository;

    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request, Authentication authentication) {
        validateSessionOwnership(request.getSessionId(), authentication.getName());
        return ResponseEntity.ok(chatService.processMessage(request.getSessionId(), request.getMessage()));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<List<ChatResponse>> getChatHistory(@PathVariable Long sessionId, Authentication authentication) {
        validateSessionOwnership(sessionId, authentication.getName());
        return ResponseEntity.ok(chatService.getChatHistory(sessionId));
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
