package com.daniel.eventManagement.dto.request.userRequest;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class UserCreateRequest {
    String email;
    String password;
    String firstName;
    String lastName;
    LocalDate birth;
    String bio;
    boolean gender;
    String address;
    String role;
}
