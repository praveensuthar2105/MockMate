package com.mockmate.security;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.debug("STOMP CONNECT received — checking Authorization header");
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.debug("Authorization header: {}",
                    authHeader != null ? "present (length=" + authHeader.length() + ")" : "MISSING");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("WebSocket CONNECT rejected: missing or invalid Authorization header");
                throw new org.springframework.messaging.MessageDeliveryException("Unauthorized");
            }

            String jwt = authHeader.substring(7);
            String userEmail = jwtService.extractUsername(jwt);
            log.debug("JWT extracted email: {}", userEmail);

            if (userEmail != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    accessor.setUser(authToken);
                    log.info("WebSocket user authenticated: {}", userEmail);
                } else {
                    log.warn("WebSocket CONNECT rejected: token invalid for user {}", userEmail);
                    throw new org.springframework.messaging.MessageDeliveryException("Unauthorized: Invalid Token");
                }
            } else {
                log.warn("WebSocket CONNECT rejected: could not extract email from JWT");
                throw new org.springframework.messaging.MessageDeliveryException("Unauthorized: Invalid Token Subject");
            }
        }
        return message;
    }
}
