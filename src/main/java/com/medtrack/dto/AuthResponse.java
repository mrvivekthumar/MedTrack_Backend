package com.medtrack.dto;

public record AuthResponse(UserDto user, String token) {
}
