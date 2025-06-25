package com.medtrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Token refresh request details")
public record RefreshTokenDto(
                @Schema(description = "Refresh token to generate new access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token_payload", required = true) @NotBlank(message = "Refresh token is required") String refreshToken) {
}
