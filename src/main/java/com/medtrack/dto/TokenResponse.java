package com.medtrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token response containing new access token")
public record TokenResponse(
                @Schema(description = "New JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new_token_payload", required = true) String token) {
}