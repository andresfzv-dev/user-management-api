package com.andres.usermanagement.service;

import com.andres.usermanagement.dto.UserRequest;
import com.andres.usermanagement.dto.UserResponse;
import com.andres.usermanagement.entity.Role;
import com.andres.usermanagement.entity.User;
import com.andres.usermanagement.exception.EmailAlreadyExistsException;
import com.andres.usermanagement.exception.ResourceNotFoundException;
import com.andres.usermanagement.repository.UserRepository;
import com.andres.usermanagement.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Andres")
                .email("andres@test.com")
                .password("encodedPassword")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        userRequest = new UserRequest();
        userRequest.setName("Andres");
        userRequest.setEmail("andres@test.com");
        userRequest.setPassword("123456");
        userRequest.setRole(Role.USER);
    }

    // --- createUser ---

    @Test
    void createUser_whenEmailNotExists_shouldReturnUserResponse() {
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.createUser(userRequest);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("andres@test.com");
        assertThat(response.getName()).isEqualTo("Andres");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_whenEmailExists_shouldThrowEmailAlreadyExistsException() {
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userRequest))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    // --- getAllUsers ---

    @Test
    void getAllUsers_shouldReturnListOfUserResponses() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> responses = userService.getAllUsers();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getEmail()).isEqualTo("andres@test.com");
    }

    @Test
    void getAllUsers_whenNoUsers_shouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponse> responses = userService.getAllUsers();

        assertThat(responses).isEmpty();
    }

    // --- getUserById ---

    @Test
    void getUserById_whenUserExists_shouldReturnUserResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("andres@test.com");
    }

    @Test
    void getUserById_whenUserNotExists_shouldThrowResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- updateUser ---

    @Test
    void updateUser_whenUserExists_shouldReturnUpdatedUserResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.updateUser(1L, userRequest);

        assertThat(response).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_whenUserNotExists_shouldThrowResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, userRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- deleteUser ---

    @Test
    void deleteUser_whenUserExists_shouldDeleteSuccessfully() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_whenUserNotExists_shouldThrowResourceNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }

    // --- getUserByEmail ---

    @Test
    void getUserByEmail_whenUserExists_shouldReturnUserResponse() {
        when(userRepository.findByEmail("andres@test.com")).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserByEmail("andres@test.com");

        assertThat(response.getEmail()).isEqualTo("andres@test.com");
    }

    @Test
    void getUserByEmail_whenUserNotExists_shouldThrowResourceNotFoundException() {
        when(userRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail("noexiste@test.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}