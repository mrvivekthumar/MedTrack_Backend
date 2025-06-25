package com.medtrack.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Health product information response with complete details")
public record HealthProductResponseDto(
        @Schema(description = "Unique health product identifier", example = "1", required = true) Long healthProductId,

        @Schema(description = "Name of the medicine/health product", example = "Paracetamol 500mg", required = true) String healthProductName,

        @Schema(description = "Total quantity originally purchased", example = "100.0", required = true) Float totalQuantity,

        @Schema(description = "Currently available quantity", example = "75.0", required = true) Float availableQuantity,

        @Schema(description = "Threshold quantity for low stock alerts", example = "10.0", required = true) Float thresholdQuantity,

        @Schema(description = "Quantity per dose", example = "1.0", required = true) Float doseQuantity,

        @Schema(description = "Unit of measurement", example = "tablets", allowableValues = {
                "tablets", "capsules", "ml", "mg", "drops", "sachets", "units" }) String unit,

        @Schema(description = "Product expiry date", example = "2025-12-31", required = true, format = "date") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate expiryDate,

        @Schema(description = "List of reminder times in HH:mm format", example = "[\"08:00\", \"14:00\", \"20:00\"]") List<String> reminderTimes,

        @Schema(description = "Date and time when the product was created", example = "2024-01-15T10:30:00", format = "date-time") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt)
        implements Serializable{

    // Compact constructor for validation if needed
    public HealthProductResponseDto {
    }

    // Constructor without availableQuantity for backward compatibility
    public HealthProductResponseDto(
            Long healthProductId,
            String healthProductName,
            Float totalQuantity,
            Float thresholdQuantity,
            Float doseQuantity,
            LocalDate expiryDate,
            String unit,
            List<String> reminderTimes) {
        this(healthProductId, healthProductName, totalQuantity, null, thresholdQuantity, doseQuantity, unit, expiryDate,
                reminderTimes, null);
    }
}