package com.mockmate.service;

import com.mockmate.dto.request.LoginRequest;
import com.mockmate.dto.request.RegisterRequest;
import com.mockmate.dto.request.RefreshTokenRequest;
import com.mockmate.dto.request.UpdateProfileRequest;
import com.mockmate.dto.response.AuthResponse;
import com.mockmate.model.User;
import com.mockmate.repository.UserRepository;
import com.mockmate.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        if (request.experienceLevel() != null) {
            user.setExperienceLevel(request.experienceLevel());
        } else {
            user.setExperienceLevel(com.mockmate.model.ExperienceLevel.FRESHER);
        }

        var savedUser = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        var jwtToken = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .experienceLevel(
                        savedUser.getExperienceLevel() != null ? savedUser.getExperienceLevel().name() : "FRESHER")
                .profileComplete(savedUser.getProfileComplete() != null ? savedUser.getProfileComplete() : false)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        var jwtToken = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .experienceLevel(user.getExperienceLevel() != null ? user.getExperienceLevel().name() : "FRESHER")
                .profileComplete(user.getProfileComplete() != null ? user.getProfileComplete() : false)
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.refreshToken();
        String userEmail = jwtService.extractUsername(token);

        if (userEmail != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(token, userDetails)) {
                var newToken = jwtService.generateToken(userDetails);
                User user = userRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));

                return AuthResponse.builder()
                        .accessToken(newToken)
                        .refreshToken(token) // Assuming we keep the old refresh token, or generate a new one
                        .userId(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .experienceLevel(
                                user.getExperienceLevel() != null ? user.getExperienceLevel().name() : "FRESHER")
                        .profileComplete(user.getProfileComplete() != null ? user.getProfileComplete() : false)
                        .build();
            }
        }
        throw new IllegalArgumentException("Invalid refresh token");
    }

    public AuthResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setName(request.name());
        user.setExperienceLevel(request.experienceLevel());
        user.setProfileComplete(true);

        var updatedUser = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(updatedUser.getEmail());
        var jwtToken = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .userId(updatedUser.getId())
                .name(updatedUser.getName())
                .email(updatedUser.getEmail())
                .experienceLevel(
                        updatedUser.getExperienceLevel() != null ? updatedUser.getExperienceLevel().name() : "FRESHER")
                .profileComplete(updatedUser.getProfileComplete() != null ? updatedUser.getProfileComplete() : false)
                .build();
    }
}
