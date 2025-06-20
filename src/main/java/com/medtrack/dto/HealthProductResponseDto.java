package com.medtrack.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public record HealthProductResponseDto(
        Long healthProductId,
        String healthProductName,
        Float totalQuantity,
        Float availableQuantity,
        Float thresholdQuantity,
        Float doseQuantity,
        String unit,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate expiryDate,
        List<String> reminderTimes,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt) implements Serializable {

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