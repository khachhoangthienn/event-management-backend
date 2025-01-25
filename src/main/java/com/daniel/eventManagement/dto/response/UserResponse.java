package com.daniel.eventManagement.dto.response;

import com.daniel.eventManagement.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse extends BaseEntity {
    String userId;
    @Column(unique = true)
    @Email
    String email;
    String firstName;
    String lastName;
    LocalDate birth;
    String bio;
    String avatarUrl;
    String role;
}
