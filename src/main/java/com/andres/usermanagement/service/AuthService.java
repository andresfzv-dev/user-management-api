package com.andres.usermanagement.service;

import com.andres.usermanagement.dto.LoginRequest;
import com.andres.usermanagement.dto.LoginResponse;
import com.andres.usermanagement.entity.RefreshToken;
import com.andres.usermanagement.entity.User;
import com.andres.usermanagement.repository.UserRepository;
import com.andres.usermanagement.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken.getToken());
    }

    public LoginResponse refresh(String token) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(token);
        User user = refreshToken.getUser();

        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return new LoginResponse(newAccessToken, refreshToken.getToken());
    }

    @Transactional
    public void logout(String token) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(token);
        refreshTokenService.deleteByUser(refreshToken.getUser());
    }
}