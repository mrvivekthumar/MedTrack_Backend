package com.medtrack.exceptions;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error response containing validation or processing errors")
public record ErrorResponse(
                @Schema(description = "Map of field names to error messages", example = "{\"email\": \"Email is required\", \"password\": \"Password must be at least 6 characters\"}") Map<String, String> errors) {
}