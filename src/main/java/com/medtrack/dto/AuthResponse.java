package com.medtrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing user details and JWT token")
public record AuthResponse(
        @Schema(description = "User profile information", required = true) UserResponseDto user,

        @Schema(description = "JWT access token for API authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTYwNjc5NjQwMCwiZXhwIjoxNjA2ODMyNDAwfQ.example", required = true) String token) {
}
