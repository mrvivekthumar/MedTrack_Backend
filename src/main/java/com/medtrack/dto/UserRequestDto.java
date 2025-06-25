package com.medtrack.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration/login request details")
public record UserRequestDto(
        @Schema(description = "Full name of the user", example = "John Doe", required = false) @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters") String fullName,

        @Schema(description = "User's email address - must be unique", example = "john.doe@example.com", required = true, format = "email") @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,

        @Schema(description = "User's password - minimum 6 characters recommended", example = "securePassword123", required = false, minLength = 6) @Size(min = 6, message = "Password must be at least 6 characters long") String password)
        implements Serializable {

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