package com.daniel.eventManagement.dto.response.authResponse;

import com.daniel.eventManagement.dto.response.UserResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    String token;
    UserResponse user;
    boolean authenticated;
}
