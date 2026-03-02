package com.andres.usermanagement.service;

import com.andres.usermanagement.dto.UserRequest;
import com.andres.usermanagement.dto.UserResponse;
import com.andres.usermanagement.entity.User;
import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest request);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserRequest request);
    void deleteUser(Long id);
}