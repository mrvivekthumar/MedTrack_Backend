package com.medtrack.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.medtrack.dto.HealthProductRequestDto;
import com.medtrack.dto.HealthProductResponseDto;
import com.medtrack.model.HealthProduct;
import com.medtrack.model.User;

/**
 * Mapper class for converting between HealthProduct entity and DTO
 */
@Component
public class HealthProductMapper {

    /*
     * Converts a HealthProduct entity to its HealthProductResponseDto
     * representation
     */
    public HealthProductResponseDto toDto(HealthProduct product) {
        if (product == null) {
            return null;
        }

        List<String> reminderTimes = product.getMedicineReminders() != null ? product.getMedicineReminders().stream()
                .map(reminder -> reminder.getTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .collect(Collectors.toList()) : Collections.emptyList();

        LocalDateTime createdAt = product.getCreatedAt() != null ? product.getCreatedAt().toLocalDateTime() : null;

        return new HealthProductResponseDto(
                product.getId(),
                product.getName(),
                product.getTotalQuantity(),
                product.getAvailableQuantity(),
                product.getThresholdQuantity(),
                product.getDoseQuantity(),
                product.getUnit(),
                product.getExpiryDate(),
                reminderTimes,
                createdAt);
    }

    /**
     * Converts a HealthProductRequestDto to its entity representation
     * Note: User and MedicineReminders need to be set separately
     */
    public HealthProduct toEntity(HealthProductRequestDto dto, User user) {
        if (dto == null) {
            return null;
        }

        return HealthProduct.builder()
                .name(dto.healthProductName())
                .totalQuantity(dto.totalQuantity())
                .availableQuantity(dto.availableQuantity())
                .thresholdQuantity(dto.thresholdQuantity())
                .doseQuantity(dto.doseQuantity())
                .expiryDate(dto.expiryDate())
                .unit(dto.unit())
                .user(user)
                .build();
    }

    /**
     * Overloaded method that doesn't require a user parameter
     * Useful for updates where we don't need to change the user
     */
    public HealthProduct toEntity(HealthProductRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return HealthProduct.builder()
                .id(dto.userId())
                .name(dto.healthProductName())
                .totalQuantity(dto.totalQuantity())
                .availableQuantity(dto.availableQuantity())
                .thresholdQuantity(dto.thresholdQuantity())
                .doseQuantity(dto.doseQuantity())
                .expiryDate(dto.expiryDate())
                .unit(dto.unit())
                .build();
    }

    /**
     * Converts a list of HealthProduct entities to DTOs
     */
    public List<HealthProductResponseDto> toDtoList(List<HealthProduct> products) {
        if (products == null) {
            return Collections.emptyList();
        }

        return products.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}