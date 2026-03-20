package com.mockmate.dto.ws;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsMessageRequest {
    private String content;
}
