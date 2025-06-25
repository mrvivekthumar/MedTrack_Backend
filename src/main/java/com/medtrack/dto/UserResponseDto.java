package com.medtrack.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User profile information response")
public record UserResponseDto(
        @Schema(description = "Unique user identifier", example = "1", required = true) Long userId,

        @Schema(description = "Full name of the user", example = "John Doe", required = true) String fullName,

        @Schema(description = "User's email address", example = "john.doe@example.com", required = true, format = "email") String email)
        implements Serializable {

    public static UserResponseDto of(Long userId, String fullName, String email) {
        return new UserResponseDto(userId, fullName, email);
    }
}
