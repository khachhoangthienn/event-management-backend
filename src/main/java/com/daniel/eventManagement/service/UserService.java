package com.daniel.eventManagement.service;

import com.daniel.eventManagement.dto.request.userRequest.UserCreateRequest;
import com.daniel.eventManagement.dto.request.userRequest.UserUpdateRequest;
import com.daniel.eventManagement.dto.response.UserResponse;
import com.daniel.eventManagement.entity.User;
import com.daniel.eventManagement.enums.Role;
import com.daniel.eventManagement.exception.AppException;
import com.daniel.eventManagement.exception.ErrorCode;
import com.daniel.eventManagement.mapper.UserMapper;
import com.daniel.eventManagement.repository.UserRepository;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreateRequest userCreateRequest) {
        User user = userMapper.toUser(userCreateRequest);
        user.setPassword(passwordEncoder.encode(userCreateRequest.getPassword()));
        if (!Role.isValidRole(user.getRole())) throw new AppException(ErrorCode.INVALID_ROLE);
        try {
            User savedUser = userRepository.save(user);
            return userMapper.toUserResponse(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    public UserResponse updateUser(String userId, UserUpdateRequest userCreateRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUser(user, userCreateRequest);
        if (!Role.isValidRole(user.getRole())) throw new AppException(ErrorCode.INVALID_ROLE);
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        System.out.println("Create at: " + user.getCreatedAt());
        return userMapper.toUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll(Sort.by(Sort.Order.asc("createdAt")));
        return users.stream().map(userMapper::toUserResponse).toList();
    }



}
