package com.andres.usermanagement.service;

import com.andres.usermanagement.entity.RefreshToken;
import com.andres.usermanagement.entity.User;
import com.andres.usermanagement.exception.ResourceNotFoundException;
import com.andres.usermanagement.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int EXPIRATION_DAYS = 7;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusDays(EXPIRATION_DAYS))
                .user(user)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired, please login again");
        }

        return refreshToken;
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteAllByUser(user);
    }
}