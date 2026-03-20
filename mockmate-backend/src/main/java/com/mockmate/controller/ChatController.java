package com.mockmate.controller;

import com.mockmate.dto.request.ChatRequest;
import com.mockmate.dto.response.ChatResponse;
import com.mockmate.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.processMessage(request.getSessionId(), request.getMessage()));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<List<ChatResponse>> getChatHistory(@PathVariable Long sessionId) {
        return ResponseEntity.ok(chatService.getChatHistory(sessionId));
    }
}
