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
import com.medtrack.mapper.HealthProductMapper;
import com.medtrack.model.HealthProduct;
import com.medtrack.model.MedicineReminder;
import com.medtrack.model.User;
import com.medtrack.repository.HealthProductRepo;
import com.medtrack.repository.UserRepo;
import com.medtrack.utils.MedicineExpiryNotificationService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Service for managing health products
 */
@Service
@RequiredArgsConstructor
public class HealthProductService {

    private final HealthProductRepo healthProductRepository;
    private final MedicineExpiryNotificationService medicineExpiryNotificationService;
    private final UserRepo userRepo;

    private static final ZoneId KOLKATA_ZONE = ZoneId.of("Asia/Kolkata");

    /**
     * Creates a new health product for a user with associated reminders
     * 
     * @param dto The health product data
     * @return The created health product as DTO
     */
    public HealthProduct createHealthProduct(HealthProductRequestDto dto) {
        User user = userRepo.findById(dto.userId())
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        System.out.println("User found: " + user.getEmail());

        // Build HealthProduct entity from DTO
        HealthProduct product = HealthProduct.builder()
                .name(dto.healthProductname())
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

        // Trigger expiry email scheduling
        medicineExpiryNotificationService.scheduleMedicineExpiryNotification(savedProduct);

        return savedProduct;
    }

    /**
     * Updates an existing health product
     * 
     * @param id  The ID of the health product to update
     * @param dto The updated health product data
     * @return The updated health product as DTO
     */
    public HealthProduct updateHealthProduct(Long healthProductId, HealthProductRequestDto dto) {
        HealthProduct existingProduct = healthProductRepository.findById(healthProductId)
                .orElseThrow(() -> new EntityNotFoundException("Health Product not found"));

        User user = existingProduct.getUser();

        // Update product fields from DTO
        existingProduct.setName(dto.healthProductname());
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

        // Update expiry notification
        medicineExpiryNotificationService.updateMedicineExpiryNotification(savedProduct);

        return savedProduct;
    }

    /**
     * Deletes a health product
     * 
     * @param id The ID of the health product to delete
     * @return true if deletion was successful
     */
    public void deleteHealthProduct(Long healthProductId) {
        HealthProduct product = healthProductRepository.findById(healthProductId)
                .orElseThrow(() -> new EntityNotFoundException("Health Product not found"));

        healthProductRepository.delete(product);

        medicineExpiryNotificationService.removeMedicineExpiryNotification(healthProductId);
    }

    /**
     * Gets a single health product by ID
     * 
     * @param id The health product ID
     * @return The health product as DTO
     */
    public HealthProduct getHealthProduct(Long healthProductId) {
        HealthProduct product = healthProductRepository.findById(healthProductId)
                .orElseThrow(() -> new EntityNotFoundException("Health Product not found"));
        return product;
    }

    /**
     * Gets all active health products for a user (with quantity > 0 and not
     * expired)
     * 
     * @param userId The user ID
     * @return List of active health products as DTOs
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
     * 
     * @param userId The user ID
     * @return List of all health products as DTOs
     */
    public List<HealthProduct> getAllHealthProducts(Long userId) {
        userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        List<HealthProduct> products = healthProductRepository.findByUserId(userId);

        return products;
    }

    /**
     * Gets health products that are below their threshold quantity
     * 
     * @param userId The user ID
     * @return List of low stock health products as DTOs
     */
    public List<HealthProduct> getLowStockHealthProducts(Long userId) {
        userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        LocalDate today = ZonedDateTime.now(KOLKATA_ZONE).toLocalDate();
        List<HealthProduct> products = healthProductRepository.findLowStockHealthProducts(userId, today);
        System.out.println("Low stock products: " + products.size());
        return products;
    }

    /**
     * Records usage of a medicine, reducing available quantity by dose amount
     * 
     * @param id The health product ID
     * @return The updated health product as DTO
     */
    public HealthProduct recordMedicineUsage(Long healthProductId) {
        HealthProduct product = healthProductRepository.findById(healthProductId)
                .orElseThrow(() -> new EntityNotFoundException("Health Product not found"));

        Float newQuantity = product.getAvailableQuantity() - product.getDoseQuantity();
        product.setAvailableQuantity(Math.max(0f, newQuantity)); // Don't go below zero

        HealthProduct updatedProduct = healthProductRepository.save(product);

        return updatedProduct;
    }
}