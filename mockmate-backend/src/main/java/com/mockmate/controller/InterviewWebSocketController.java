package com.mockmate.controller;

import com.mockmate.dto.response.ChatResponse;
import com.mockmate.dto.ws.WsEvent;
import com.mockmate.dto.ws.WsMessageRequest;
import com.mockmate.model.InterviewSession;
import com.mockmate.model.PhaseType;
import com.mockmate.repository.InterviewSessionRepository;
import com.mockmate.service.ChatService;
import com.mockmate.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
@org.springframework.transaction.annotation.Transactional
public class InterviewWebSocketController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;
    private final InterviewService interviewService;
    private final InterviewSessionRepository sessionRepository;

    @MessageMapping("/interview/{sessionId}/message")
    public void handleMessage(
            @DestinationVariable Long sessionId,
            @Payload WsMessageRequest request,
            Principal principal) {

        log.debug("WebSocket message received for session {}, content: {}", sessionId,
                request != null ? request.getContent() : "null");

        // Check if user is authenticated
        if (principal == null) {
            log.error("Principal is null — user is not authenticated via WebSocket");
            simpMessagingTemplate.convertAndSend(
                    "/topic/session/" + sessionId,
                    WsEvent.error("Not authenticated. Please reconnect with a valid token."));
            return;
        }

        log.debug("Authenticated user: {}", principal.getName());

        // Step 1: Validate session belongs to authenticated user
        InterviewSession session = validateSessionOwnership(sessionId, principal.getName());

        // Step 2: Publish typing indicator immediately
        simpMessagingTemplate.convertAndSend(
                "/topic/session/" + sessionId,
                WsEvent.typing());

        try {
            // Step 3 & 4: Save user message and call ChatService for AI response
            ChatResponse aiResponse = chatService.processMessage(sessionId, request.getContent());

            // Step 5: Broadcast AI response
            simpMessagingTemplate.convertAndSend(
                    "/topic/session/" + sessionId,
                    WsEvent.message(aiResponse.getContent()));

            // Step 6: Check if phase should end
            // (e.g. max questions or follow-up count reached).
            // This is a placeholder for custom follow-up checking logic.
            boolean followUpLimitReached = false;
            if (aiResponse.getContent().toLowerCase().contains("move to the next round") ||
                    aiResponse.getContent().toLowerCase().contains("move to the next section")) {
                followUpLimitReached = true;
            }

            if (followUpLimitReached) {
                // If the AI naturally concluded the phase, we could auto-advance here
                // simpMessagingTemplate.convertAndSend("/topic/session/" + sessionId,
                // WsEvent.phaseChange(session.getCurrentPhase()));
            }

        } catch (Exception e) {
            log.error("Error processing websocket message", e);
            simpMessagingTemplate.convertAndSend(
                    "/topic/session/" + sessionId,
                    WsEvent.error("Failed to process message"));
        }
    }

    @MessageMapping("/interview/{sessionId}/phase-complete")
    public void handlePhaseComplete(
            @DestinationVariable Long sessionId,
            Principal principal) {

        // Step 1: Validate session ownership
        validateSessionOwnership(sessionId, principal.getName());

        try {
            // Step 2: Call InterviewService.advancePhase(sessionId)
            // (Implemented in Task 6 in InterviewService)
            PhaseType newPhase = interviewService.advancePhase(sessionId);

            // Step 3: Broadcast PHASE_CHANGE event with new phase details
            simpMessagingTemplate.convertAndSend(
                    "/topic/session/" + sessionId,
                    WsEvent.phaseChange(newPhase));

        } catch (Exception e) {
            log.error("Error advancing phase", e);
            simpMessagingTemplate.convertAndSend(
                    "/topic/session/" + sessionId,
                    WsEvent.error("Failed to advance phase"));
        }
    }

    private InterviewSession validateSessionOwnership(Long sessionId, String username) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (!session.getUser().getEmail().equals(username)) {
            throw new SecurityException("Unauthorized access to session");
        }
        return session;
    }
}
