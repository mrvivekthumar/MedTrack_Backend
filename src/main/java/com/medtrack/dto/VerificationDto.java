package com.medtrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Email verification request details")
public record VerificationDto(
                @Schema(description = "6-digit verification code sent to email", example = "123456", required = true, pattern = "^[0-9]{6}$") @NotBlank(message = "Verification code is required") @Pattern(regexp = "^[0-9]{6}$", message = "Verification code must be 6 digits") String code) {
}