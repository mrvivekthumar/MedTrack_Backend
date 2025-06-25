package com.medtrack.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Health product creation/update request details")
public record HealthProductRequestDto(
        @Schema(description = "User ID who owns this health product", example = "1", required = true) @NotNull(message = "User ID is required") Long userId,

        @Schema(description = "Name of the medicine/health product", example = "Paracetamol 500mg", required = true) @NotBlank(message = "Health product name is required") String healthProductName,

        @Schema(description = "Total quantity of the product purchased/available", example = "100.0", required = true) @NotNull(message = "Total quantity is required") @Positive(message = "Total quantity must be greater than 0") Float totalQuantity,

        @Schema(description = "Currently available quantity (auto-calculated if not provided)", example = "75.0", required = false) @PositiveOrZero(message = "Available quantity cannot be negative") Float availableQuantity,

        @Schema(description = "Threshold quantity for low stock alerts", example = "10.0", required = false) @PositiveOrZero(message = "Threshold quantity cannot be negative") Float thresholdQuantity,

        @Schema(description = "Quantity per dose (how much to take each time)", example = "1.0", required = true) @NotNull(message = "Dose quantity is required") @Positive(message = "Dose quantity must be greater than 0") Float doseQuantity,

        @Schema(description = "Unit of measurement for the product", example = "tablets", allowableValues = {
                "tablets", "capsules", "ml", "mg", "drops", "sachets", "units" }) String unit,

        @Schema(description = "Expiry date of the product", example = "2025-12-31", required = true, format = "date") @NotNull(message = "Expiry date is required") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate expiryDate,

        @Schema(description = "List of reminder times in HH:mm format", example = "[\"08:00\", \"14:00\", \"20:00\"]", required = false) List<String> reminderTimes)
        implements Serializable{

    // Compact constructor for validation if needed
    public HealthProductRequestDto {
    }

    // Constructor without availableQuantity for backward compatibility
    public HealthProductRequestDto(
            Long userId,
            String healthProductname,
            Float totalQuantity,
            Float thresholdQuantity,
            Float doseQuantity,
            LocalDate expiryDate,
            String unit,
            List<String> reminderTimes) {
        this(userId, healthProductname, totalQuantity, null, thresholdQuantity, doseQuantity, unit, expiryDate,
                reminderTimes);
    }
}