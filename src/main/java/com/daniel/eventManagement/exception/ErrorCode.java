package com.daniel.eventManagement.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {

    // 5xx Server Errors
    UNCATEGORIZED_EXCEPTION(500, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    ENDPOINT_NOT_FOUND(404, "Not found: Endpoint was not found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(404, "Not found: User was not found", HttpStatus.NOT_FOUND),
    // 4xx Client Errors
    USERNAME_EXISTED(409, "Conflict: Username already exists", HttpStatus.CONFLICT),
    UNAUTHENTICATED(401, "Unauthorized: Authentication is required", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(401, "Invalid token", HttpStatus.UNAUTHORIZED),
    INVALID_REQUEST_DATA(400, "Bad request: Invalid request data", HttpStatus.BAD_REQUEST),
    INVALID_DATE_FORMAT(400, "Invalid date format: Failed to parse date. Expected format is yyyy-MM-dd.", HttpStatus.BAD_REQUEST),
    INVALID_ARGUMENT(400, "Invalid argument provided", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED(403, "Forbidden: Access denied", HttpStatus.FORBIDDEN),
    INVALID_PASSWORD(401, "Invalid password: Password is incorrect", HttpStatus.UNAUTHORIZED),
    OTP_NOT_VERIFIED(401, "Unauthorized: OTP not verified", HttpStatus.UNAUTHORIZED),
    INVALID_OTP(400, "Invalid OTP: The provided OTP is incorrect", HttpStatus.BAD_REQUEST),
    INVALID_ROLE(400, "Invalid role: The provided role is invalid", HttpStatus.BAD_REQUEST),
    ;
    final int code;
    String message;
    private HttpStatusCode statusCode;
}
