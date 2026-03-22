package com.mockmate.dto.response;

import com.mockmate.model.PhaseType;
import com.mockmate.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private Role role;
    private String content;
    private LocalDateTime timestamp;
    private PhaseType phase;
}
