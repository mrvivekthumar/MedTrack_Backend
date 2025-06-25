package com.medtrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Password reset request details")
public record ForgotPasswordDto(
                @Schema(description = "Email address for password reset", example = "john.doe@example.com", required = true, format = "email") @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email) {
}