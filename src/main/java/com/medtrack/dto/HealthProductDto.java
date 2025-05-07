package com.medtrack.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;


public record HealthProductDto(
        Long id,
        Long userId,
        String name,
        Float totalQuantity,
        Float availableQuantity,
        Float thresholdQuantity,
        Float doseQuantity,
        String unit,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate expiryDate,
        List<String> reminderTimes) implements Serializable {

    // Compact constructor for validation if needed
    public HealthProductDto {
    }

    // Constructor without availableQuantity for backward compatibility
    public HealthProductDto(
            Long id,
            Long userId,
            String name,
            Float totalQuantity,
            Float thresholdQuantity,
            Float doseQuantity,
            LocalDate expiryDate,
            String unit,
            List<String> reminderTimes) {
        this(id, userId, name, totalQuantity, null, thresholdQuantity, doseQuantity, unit, expiryDate, reminderTimes);
    }
}