package com.medtrack.dto;

import java.io.Serializable;

public record UserRequestDto(
        String fullName,
        String email,
        String password) implements Serializable {

    // Static factory method for builder-like syntax
    public static UserRequestDto of(String fullName, String email, String password) {
        return new UserRequestDto(fullName, email, password);
    }
}

/**
 * Data Transfer Object for User entity
 * Records provide a concise way to create immutable DTOs with built-in:
 * - Constructor
 * - equals/hashCode
 * - toString
 * - Getters (in the form of accessor methods)
 * 
 * - No setters (immutable). it doesnt gives setters because of immutability
 */