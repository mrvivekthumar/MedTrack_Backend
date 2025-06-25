package com.medtrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Password change request details")
public record ChangePasswordDto(
                @Schema(description = "Current password for verification", example = "oldPassword123", required = true) @NotBlank(message = "Current password is required") String currentPassword,

                @Schema(description = "New password - minimum 6 characters recommended", example = "newSecurePassword456", required = true, minLength = 6) @NotBlank(message = "New password is required") @Size(min = 6, message = "New password must be at least 6 characters long") String newPassword) {
}