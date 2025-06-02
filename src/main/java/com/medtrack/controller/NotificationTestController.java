// Create this file: src/main/java/com/medtrack/controller/NotificationTestController.java
package com.medtrack.controller;

import com.medtrack.kafka.service.NotificationProducerService;
import com.medtrack.model.HealthProduct;
import com.medtrack.model.User;
import com.medtrack.repository.HealthProductRepo;
import com.medtrack.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationTestController {

    private final NotificationProducerService notificationProducerService;
    private final HealthProductRepo healthProductRepo;
    private final UserRepo userRepo;

    /**
     * Test endpoint to manually trigger an expiry notification
     */
    @PostMapping("/test-expiry/{productId}")
    public ResponseEntity<Map<String, String>> testExpiryNotification(@PathVariable Long productId) {
        Map<String, String> response = new HashMap<>();

        try {
            HealthProduct product = healthProductRepo.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            notificationProducerService.sendExpiryNotification(product);

            response.put("status", "success");
            response.put("message", "Expiry notification sent to Kafka for product: " + product.getName());
            response.put("productName", product.getName());
            response.put("userEmail", product.getUser().getEmail());

            log.info("Test expiry notification sent for product: {}", product.getName());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to send notification: " + e.getMessage());
            log.error("Test expiry notification failed", e);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Test endpoint to manually trigger a low stock notification
     */
    @PostMapping("/test-low-stock/{productId}")
    public ResponseEntity<Map<String, String>> testLowStockNotification(@PathVariable Long productId) {
        Map<String, String> response = new HashMap<>();

        try {
            HealthProduct product = healthProductRepo.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            notificationProducerService.sendLowStockNotification(product);

            response.put("status", "success");
            response.put("message", "Low stock notification sent to Kafka for product: " + product.getName());
            response.put("productName", product.getName());
            response.put("availableQuantity", product.getAvailableQuantity().toString());
            response.put("thresholdQuantity", product.getThresholdQuantity().toString());

            log.info("Test low stock notification sent for product: {}", product.getName());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to send notification: " + e.getMessage());
            log.error("Test low stock notification failed", e);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Test endpoint to manually trigger an out of stock notification
     */
    @PostMapping("/test-out-of-stock/{productId}")
    public ResponseEntity<Map<String, String>> testOutOfStockNotification(@PathVariable Long productId) {
        Map<String, String> response = new HashMap<>();

        try {
            HealthProduct product = healthProductRepo.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            notificationProducerService.sendOutOfStockNotification(product);

            response.put("status", "success");
            response.put("message", "Out of stock notification sent to Kafka for product: " + product.getName());
            response.put("productName", product.getName());

            log.info("Test out of stock notification sent for product: {}", product.getName());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to send notification: " + e.getMessage());
            log.error("Test out of stock notification failed", e);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Test Kafka connectivity
     */
    @GetMapping("/health-check")
    public ResponseEntity<Map<String, Object>> kafkaHealthCheck() {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isHealthy = notificationProducerService.isKafkaHealthy();

            response.put("status", isHealthy ? "healthy" : "unhealthy");
            response.put("kafka", isHealthy ? "connected" : "disconnected");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "unhealthy");
            response.put("kafka", "error");
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Create a test product for testing notifications
     */
    @PostMapping("/create-test-product/{userId}")
    public ResponseEntity<Map<String, Object>> createTestProduct(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            HealthProduct testProduct = HealthProduct.builder()
                    .name("Test Medicine - " + System.currentTimeMillis())
                    .totalQuantity(100f)
                    .availableQuantity(15f) // Low stock for testing
                    .thresholdQuantity(20f)
                    .doseQuantity(5f)
                    .unit("tablets")
                    .expiryDate(LocalDate.now().plusDays(3)) // Expires in 3 days
                    .user(user)
                    .build();

            HealthProduct savedProduct = healthProductRepo.save(testProduct);

            response.put("status", "success");
            response.put("message", "Test product created successfully");
            response.put("productId", savedProduct.getId());
            response.put("productName", savedProduct.getName());
            response.put("expiryDate", savedProduct.getExpiryDate().toString());
            response.put("availableQuantity", savedProduct.getAvailableQuantity());

            log.info("Test product created: {} (ID: {})", savedProduct.getName(), savedProduct.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to create test product: " + e.getMessage());
            log.error("Test product creation failed", e);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get all products for a user (for testing)
     */
    @GetMapping("/products/{userId}")
    public ResponseEntity<Object> getUserProducts(@PathVariable Long userId) {
        try {
            var products = healthProductRepo.findByUserId(userId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}