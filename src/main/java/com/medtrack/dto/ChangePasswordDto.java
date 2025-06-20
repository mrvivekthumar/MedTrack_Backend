package com.medtrack.dto;

public record ChangePasswordDto(
        String currentPassword,
        String newPassword) {
}