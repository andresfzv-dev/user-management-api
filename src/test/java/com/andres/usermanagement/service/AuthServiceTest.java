package com.andres.usermanagement.service;

import com.andres.usermanagement.dto.LoginRequest;
import com.andres.usermanagement.dto.LoginResponse;
import com.andres.usermanagement.entity.RefreshToken;
import com.andres.usermanagement.entity.Role;
import com.andres.usermanagement.entity.User;
import com.andres.usermanagement.repository.UserRepository;
import com.andres.usermanagement.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private RefreshToken refreshToken;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Andres")
                .email("andres@test.com")
                .password("encodedPassword")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();

        refreshToken = RefreshToken.builder()
                .id(1L)
                .token("uuid-refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .user(user)
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("andres@test.com");
        loginRequest.setPassword("123456");
    }

    // --- login ---

    @Test
    void login_whenValidCredentials_shouldReturnLoginResponse() {
        when(userRepository.findByEmail("andres@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

        LoginResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("uuid-refresh-token");
    }

    @Test
    void login_whenUserNotFound_shouldThrowRuntimeException() {
        when(userRepository.findByEmail("andres@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid credentials");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_whenWrongPassword_shouldThrowRuntimeException() {
        when(userRepository.findByEmail("andres@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid credentials");

        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    // --- refresh ---

    @Test
    void refresh_whenValidToken_shouldReturnNewAccessToken() {
        when(refreshTokenService.validateRefreshToken("uuid-refresh-token")).thenReturn(refreshToken);
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("new-access-token");

        LoginResponse response = authService.refresh("uuid-refresh-token");

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("uuid-refresh-token");
    }

    @Test
    void refresh_whenInvalidToken_shouldThrowRuntimeException() {
        when(refreshTokenService.validateRefreshToken("invalid-token"))
                .thenThrow(new RuntimeException("Refresh token not found"));

        assertThatThrownBy(() -> authService.refresh("invalid-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Refresh token not found");
    }

    // --- logout ---

    @Test
    void logout_whenValidToken_shouldDeleteAllUserTokens() {
        when(refreshTokenService.validateRefreshToken("uuid-refresh-token")).thenReturn(refreshToken);

        authService.logout("uuid-refresh-token");

        verify(refreshTokenService).deleteByUser(user);
    }

    @Test
    void logout_whenInvalidToken_shouldThrowRuntimeException() {
        when(refreshTokenService.validateRefreshToken("invalid-token"))
                .thenThrow(new RuntimeException("Refresh token not found"));

        assertThatThrownBy(() -> authService.logout("invalid-token"))
                .isInstanceOf(RuntimeException.class);

        verify(refreshTokenService, never()).deleteByUser(any());
    }
}