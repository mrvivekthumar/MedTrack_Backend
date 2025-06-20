// Updated HealthProductService.java - Replace your existing one
package com.medtrack.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.medtrack.dto.HealthProductRequestDto;
import com.medtrack.exceptions.AuthException;
import com.medtrack.kafka.service.NotificationProducerService;
import com.medtrack.model.HealthProduct;
import com.medtrack.model.MedicineReminder;
import com.medtrack.model.User;
import com.medtrack.repository.HealthProductRepo;
import com.medtrack.repository.UserRepo;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing health products with Kafka-based notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HealthProductService {

    private final HealthProductRepo healthProductRepository;
    private final NotificationProducerService notificationProducerService; // NEW: Kafka producer
    private final UserRepo userRepo;

    private static final ZoneId KOLKATA_ZONE = ZoneId.of("Asia/Kolkata");

    /**
     * Creates a new health product for a user with associated reminders
     */
    @Transactional
    public HealthProduct createHealthProduct(HealthProductRequestDto dto) {
        User user = userRepo.findById(dto.userId())
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        log.info("Creating health product for user: {}", user.getEmail());

        // Build HealthProduct entity from DTO
        HealthProduct product = HealthProduct.builder()
                .name(dto.healthProductName())
                .totalQuantity(dto.totalQuantity())
                .availableQuantity(dto.totalQuantity()) // Initially, available equals total
                .thresholdQuantity(
                        dto.thresholdQuantity() != null ? dto.thresholdQuantity() : dto.totalQuantity() * 0.1f)
                .doseQuantity(dto.doseQuantity())
                .expiryDate(dto.expiryDate())
                .unit(dto.unit())
                .user(user)
                .build();

        // Map reminder times from DTO to MedicineReminder entities
        if (dto.reminderTimes() != null && !dto.reminderTimes().isEmpty()) {
            Set<MedicineReminder> reminders = dto.reminderTimes().stream()
                    .map(timeStr -> {
                        MedicineReminder reminder = new MedicineReminder();
                        reminder.setTime(LocalTime.parse(timeStr));
                        reminder.setHealthProduct(product);
                        return reminder;
                    })
                    .collect(Collectors.toSet());

            product.setMedicineReminders(reminders);
        }

        HealthProduct savedProduct = healthProductRepository.save(product);

        // NEW: Send expiry notification to Kafka instead of old notification service
        try {
            notificationProducerService.sendExpiryNotification(savedProduct);
            log.info("Expiry notification queued for product: {}", savedProduct.getName());
        } catch (Exception e) {
            log.error("Failed to queue expiry notification for product: {}", savedProduct.getName(), e);
            // Don't fail the entire operation if notification fails
        }

        return savedProduct;
    }

    /**
     * Updates an existing health product
     */
    @Transactional
    public HealthProduct updateHealthProduct(Long healthProductId, HealthProductRequestDto dto) {
        HealthProduct existingProduct = healthProductRepository.findById(healthProductId)
                .orElseThrow(() -> new EntityNotFoundException("Health Product not found"));

        User user = existingProduct.getUser();

        // Update product fields from DTO
        existingProduct.setName(dto.healthProductName());
        existingProduct.setTotalQuantity(dto.totalQuantity());

        // Only update available quantity if specifically provided
        if (dto.availableQuantity() != null) {
            existingProduct.setAvailableQuantity(dto.availableQuantity());
        }

        existingProduct.setThresholdQuantity(dto.thresholdQuantity());
        existingProduct.setDoseQuantity(dto.doseQuantity());
        existingProduct.setExpiryDate(dto.expiryDate());
        existingProduct.setUnit(dto.unit());

        // Handle reminder updates if provided
        if (dto.reminderTimes() != null) {
            // Clear existing reminders
            existingProduct.getMedicineReminders().clear();

            // Add new reminders
            Set<MedicineReminder> reminders = dto.reminderTimes().stream()
                    .map(timeStr -> {
                        MedicineReminder reminder = new MedicineReminder();
                        reminder.setTime(LocalTime.parse(timeStr));
                        reminder.setHealthProduct(existingProduct);
                        return reminder;
                    })
                    .collect(Collectors.toSet());

            existingProduct.setMedicineReminders(reminders);
        }

        HealthProduct savedProduct = healthProductRepository.save(existingProduct);

        // NEW: Update expiry notification in Kafka
        try {
            notificationProducerService.sendExpiryNotification(savedProduct);
            log.info("Updated expiry notification queued for product: {}", savedProduct.getName());
        } catch (Exception e) {
            log.error("Failed to update expiry notification for product: {}", savedProduct.getName(), e);
        }

        return savedProduct;
    }

    /**
     * Deletes a health product
     */
    @Transactional
    public void deleteHealthProduct(Long healthProductId) {
        HealthProduct product = healthProductRepository.findById(healthProductId)
                .orElseThrow(() -> new EntityNotFoundException("Health Product not found"));

        healthProductRepository.delete(product);

        // NOTE: We don't need to explicitly cancel notifications in Kafka
        // The consumer will handle non-existent products gracefully
        log.info("Deleted health product: {} (ID: {})", product.getName(), healthProductId);
    }

    /**
     * Gets a single health product by ID
     */
    public HealthProduct getHealthProduct(Long healthProductId) {
        HealthProduct product = healthProductRepository.findById(healthProductId)
                .orElseThrow(() -> new EntityNotFoundException("Health Product not found"));
        return product;
    }

    /**
     * Gets all active health products for a user (with quantity > 0 and not
     * expired)
     */
    public List<HealthProduct> getActiveHealthProducts(Long userId) {
        userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        LocalDate today = ZonedDateTime.now(KOLKATA_ZONE).toLocalDate();
        List<HealthProduct> products = healthProductRepository
                .findAllByUserIdAndAvailableQuantityGreaterThanAndExpiryDateAfter(userId, 0f, today);

        return products;
    }

    /**
     * Gets all health products for a user, including expired and zero-quantity
     * items
     */
    public List<HealthProduct> getAllHealthProducts(Long userId) {
        userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        List<HealthProduct> products = healthProductRepository.findByUserId(userId);

        return products;
    }

    /**
     * Gets health products that are below their threshold quantity
     */
    public List<HealthProduct> getLowStockHealthProducts(Long userId) {
        userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        LocalDate today = ZonedDateTime.now(KOLKATA_ZONE).toLocalDate();
        List<HealthProduct> products = healthProductRepository.findLowStockHealthProducts(userId, today);

        // NEW: Send low stock notifications to Kafka
        for (HealthProduct product : products) {
            try {
                notificationProducerService.sendLowStockNotification(product);
                log.debug("Low stock notification queued for product: {}", product.getName());
            } catch (Exception e) {
                log.error("Failed to queue low stock notification for product: {}", product.getName(), e);
            }
        }

        log.info("Found {} low stock products for user: {}", products.size(), userId);
        return products;
    }

    /**
     * Records usage of a medicine, reducing available quantity by dose amount
     */
    @Transactional
    public HealthProduct recordMedicineUsage(Long healthProductId) {
        HealthProduct product = healthProductRepository.findById(healthProductId)
                .orElseThrow(() -> new EntityNotFoundException("Health Product not found"));

        if (product.getAvailableQuantity() < product.getDoseQuantity()) {
            throw new AuthException("Insufficient quantity available for dose");
        }

        Float originalQuantity = product.getAvailableQuantity();
        Float newQuantity = originalQuantity - product.getDoseQuantity();
        product.setAvailableQuantity(Math.max(0f, newQuantity)); // Don't go below zero

        HealthProduct updatedProduct = healthProductRepository.save(product);

        // NEW: Check for low stock or out of stock and send notifications
        try {
            if (updatedProduct.getAvailableQuantity() <= 0) {
                notificationProducerService.sendOutOfStockNotification(updatedProduct);
                log.info("Out of stock notification queued for product: {}", updatedProduct.getName());
            } else if (updatedProduct.getAvailableQuantity() <= updatedProduct.getThresholdQuantity()) {
                notificationProducerService.sendLowStockNotification(updatedProduct);
                log.info("Low stock notification queued for product: {}", updatedProduct.getName());
            }
        } catch (Exception e) {
            log.error("Failed to queue stock notification for product: {}", updatedProduct.getName(), e);
        }

        log.info("Medicine usage recorded for product: {} (quantity: {} -> {})",
                product.getName(), originalQuantity, updatedProduct.getAvailableQuantity());

        return updatedProduct;
    }
}