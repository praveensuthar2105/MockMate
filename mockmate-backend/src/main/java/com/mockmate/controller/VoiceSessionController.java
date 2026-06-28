package com.mockmate.controller;

import com.mockmate.dto.response.VoiceSessionTokenResponse;
import com.mockmate.dto.response.ChatResponse;
import com.mockmate.dto.request.VoiceTranscriptRequest;
import jakarta.validation.Valid;
import com.mockmate.service.VoiceSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions/{sessionId}/voice")
@RequiredArgsConstructor
public class VoiceSessionController {

    private final VoiceSessionService voiceSessionService;

    @PostMapping("/token")
    public ResponseEntity<VoiceSessionTokenResponse> createVoiceToken(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        VoiceSessionTokenResponse token =
                voiceSessionService.createToken(sessionId, userDetails.getUsername());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(token);
    }

    @PostMapping("/transcript")
    public ResponseEntity<ChatResponse> saveTranscript(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @org.springframework.web.bind.annotation.RequestBody VoiceTranscriptRequest request) {
        return ResponseEntity.ok(voiceSessionService.saveTranscript(
                sessionId,
                userDetails.getUsername(),
                request.role(),
                request.content()));
    }
}
