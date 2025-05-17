package com.medtrack.dto;

import java.io.Serializable;

public record UserResponseDto(
        Long userId,
        String fullName,
        String email) implements Serializable {

    public static UserResponseDto of(Long userId, String fullName, String email) {
        return new UserResponseDto(userId, fullName, email);
    }
}
