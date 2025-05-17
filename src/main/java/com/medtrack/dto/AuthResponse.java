package com.medtrack.dto;

public record AuthResponse(UserResponseDto user, String token) {
}
