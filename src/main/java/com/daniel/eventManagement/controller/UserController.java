package com.daniel.eventManagement.controller;

import com.daniel.eventManagement.dto.request.userRequest.UserCreateRequest;
import com.daniel.eventManagement.dto.request.userRequest.UserUpdateRequest;
import com.daniel.eventManagement.dto.response.ApiResponse;
import com.daniel.eventManagement.dto.response.UserResponse;
import com.daniel.eventManagement.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(UserCreateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @PutMapping
    public ApiResponse<UserResponse> updateUser(String userId, UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUserById(@PathVariable String userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserById(userId))
                .build();
    }
}
