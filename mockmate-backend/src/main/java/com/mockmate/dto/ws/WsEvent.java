package com.mockmate.dto.ws;

import com.mockmate.model.PhaseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsEvent {

    private String type; // MESSAGE, TYPING, PHASE_CHANGE, TIMER_UPDATE, ERROR
    private String content;
    private String role; // USER, AI
    private PhaseType phase;
    private Integer timeRemainingSeconds;
    private LocalDateTime timestamp;

    public static WsEvent typing() {
        return WsEvent.builder()
                .type("TYPING")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static WsEvent message(String content) {
        return WsEvent.builder()
                .type("MESSAGE")
                .content(content)
                .role("AI")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static WsEvent phaseChange(PhaseType phase) {
        return WsEvent.builder()
                .type("PHASE_CHANGE")
                .phase(phase)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static WsEvent timerUpdate(int seconds) {
        return WsEvent.builder()
                .type("TIMER_UPDATE")
                .timeRemainingSeconds(seconds)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static WsEvent error(String message) {
        return WsEvent.builder()
                .type("ERROR")
                .content(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
