package com.daniel.eventManagement.mapper;

import com.daniel.eventManagement.dto.request.userRequest.UserCreateRequest;
import com.daniel.eventManagement.dto.request.userRequest.UserUpdateRequest;
import com.daniel.eventManagement.dto.response.UserResponse;
import com.daniel.eventManagement.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreateRequest request);
    UserResponse toUserResponse(User user);
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
