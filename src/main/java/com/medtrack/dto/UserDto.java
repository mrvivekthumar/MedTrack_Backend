package com.medtrack.dto;

import java.io.Serializable;

public record UserDto(
        Long id,
        String name,
        String email) implements Serializable {

    // The canonical constructor is automatically provided by the record
    // No need to explicitly define it

    // Static factory method for builder-like syntax if needed
    public static UserDto of(Long id, String name, String email) {
        return new UserDto(id, name, email);
    }
}

/**
 * Data Transfer Object for User entity
 * Records provide a concise way to create immutable DTOs with built-in:
 * - Constructor
 * - equals/hashCode
 * - toString
 * - Getters (in the form of accessor methods)
 */