package com.medtrack.controller;

import com.medtrack.kafka.service.NotificationProducerService;
import com.medtrack.model.HealthProduct;
import com.medtrack.model.User;
import com.medtrack.repository.HealthProductRepo;
import com.medtrack.repository.UserRepo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Notification Testing", description = "Testing endpoints for Kafka-based notification system - Development and QA use only")
@SecurityRequirement(name = "bearerAuth")
public class NotificationTestController {

    private final NotificationProducerService notificationProducerService;
    private final HealthProductRepo healthProductRepo;
    private final UserRepo userRepo;

    @PostMapping("/test-expiry/{productId}")
    @Operation(summary = "🧪 Test expiry notification", description = """
            **FOR TESTING ONLY** - Manually triggers an expiry notification for a specific product.

            This endpoint:
            - Sends expiry warning message to Kafka topic
            - Uses the actual notification system pipeline
            - Helps verify email delivery and message formatting

            ⚠️ **Note**: This will send a real email if the notification consumer is running!
            """, tags = { "Manual Testing" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expiry notification sent successfully to Kafka", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "status": "success",
                        "message": "Expiry notification sent to Kafka for product: Paracetamol 500mg",
                        "productName": "Paracetamol 500mg",
                        "userEmail": "john.doe@example.com"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Product not found or Kafka error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "status": "error",
                        "message": "Failed to send notification: Product not found"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Map<String, String>> testExpiryNotification(
            @Parameter(description = "Health product ID to test expiry notification for", required = true, example = "1") @PathVariable Long productId) {
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

    @PostMapping("/test-low-stock/{productId}")
    @Operation(summary = "🧪 Test low stock notification", description = """
            **FOR TESTING ONLY** - Manually triggers a low stock alert for a specific product.

            This endpoint:
            - Sends low stock warning to Kafka topic
            - Includes current availability and threshold info
            - Tests the complete notification pipeline

            ⚠️ **Note**: This will send a real email if the notification consumer is running!
            """, tags = { "Manual Testing" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Low stock notification sent successfully to Kafka", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "status": "success",
                        "message": "Low stock notification sent to Kafka for product: Paracetamol 500mg",
                        "productName": "Paracetamol 500mg",
                        "availableQuantity": "5.0",
                        "thresholdQuantity": "10.0"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Product not found or Kafka error", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Map<String, String>> testLowStockNotification(
            @Parameter(description = "Health product ID to test low stock notification for", required = true, example = "1") @PathVariable Long productId) {
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

    @PostMapping("/test-out-of-stock/{productId}")
    @Operation(summary = "🧪 Test out of stock notification", description = """
            **FOR TESTING ONLY** - Manually triggers an out of stock alert for a specific product.

            This endpoint:
            - Sends critical out of stock alert to Kafka
            - Marks product as completely unavailable
            - Tests urgent notification delivery

            ⚠️ **Note**: This will send a real email if the notification consumer is running!
            """, tags = { "Manual Testing" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Out of stock notification sent successfully to Kafka", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "status": "success",
                        "message": "Out of stock notification sent to Kafka for product: Paracetamol 500mg",
                        "productName": "Paracetamol 500mg"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Product not found or Kafka error", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Map<String, String>> testOutOfStockNotification(
            @Parameter(description = "Health product ID to test out of stock notification for", required = true, example = "1") @PathVariable Long productId) {
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

    @GetMapping("/health-check")
    @Operation(summary = "🔍 Check Kafka connectivity", description = """
            Performs a health check of the Kafka notification system.

            **Checks:**
            - Kafka broker connectivity
            - Producer functionality
            - Topic accessibility

            **Use this to:**
            - Verify Kafka is running and accessible
            - Test message production capabilities
            - Debug notification system issues
            """, tags = { "System Health" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kafka system is healthy", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "status": "healthy",
                        "kafka": "connected",
                        "timestamp": 1706198400000
                    }
                    """))),
            @ApiResponse(responseCode = "500", description = "Kafka system is unhealthy", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "status": "unhealthy",
                        "kafka": "error",
                        "error": "Connection refused to Kafka broker",
                        "timestamp": 1706198400000
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
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

    @PostMapping("/create-test-product/{userId}")
    @Operation(summary = "🧪 Create test product", description = """
            **FOR TESTING ONLY** - Creates a test medicine product with pre-configured values for testing notifications.

            **Created product features:**
            - Low stock threshold for testing low stock alerts
            - Near expiry date for testing expiry notifications
            - Realistic quantities and dosing information

            **Perfect for:**
            - QA testing of notification flows
            - Demo purposes
            - Integration testing
            """, tags = { "Test Data Creation" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Test product created successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "status": "success",
                        "message": "Test product created successfully",
                        "productId": 123,
                        "productName": "Test Medicine - 1706198400000",
                        "expiryDate": "2024-04-15",
                        "availableQuantity": 15.0
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "User not found or creation failed", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "status": "error",
                        "message": "Failed to create test product: User not found"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Map<String, Object>> createTestProduct(
            @Parameter(description = "User ID to create test product for", required = true, example = "1") @PathVariable Long userId) {
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

    @GetMapping("/products/{userId}")
    @Operation(summary = "📋 Get user products for testing", description = """
            **FOR TESTING ONLY** - Retrieves all products for a user to help with testing workflows.

            **Useful for:**
            - Finding product IDs for notification testing
            - Verifying test product creation
            - Checking product states before/after tests

            **Returns:** Complete product list with IDs, names, quantities, and expiry dates.
            """, tags = { "Test Data Retrieval" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User products retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(type = "array"), examples = @ExampleObject(value = """
                    [
                        {
                            "id": 1,
                            "name": "Paracetamol 500mg",
                            "totalQuantity": 100.0,
                            "availableQuantity": 75.0,
                            "thresholdQuantity": 10.0,
                            "doseQuantity": 1.0,
                            "unit": "tablets",
                            "expiryDate": "2025-12-31",
                            "user": {
                                "id": 1,
                                "fullname": "John Doe",
                                "email": "john.doe@example.com"
                            }
                        },
                        {
                            "id": 2,
                            "name": "Test Medicine - 1706198400000",
                            "totalQuantity": 100.0,
                            "availableQuantity": 15.0,
                            "thresholdQuantity": 20.0,
                            "doseQuantity": 5.0,
                            "unit": "tablets",
                            "expiryDate": "2024-04-15",
                            "user": {
                                "id": 1,
                                "fullname": "John Doe",
                                "email": "john.doe@example.com"
                            }
                        }
                    ]
                    """))),
            @ApiResponse(responseCode = "400", description = "Error retrieving products", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "status": "error",
                        "message": "User not found"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Object> getUserProducts(
            @Parameter(description = "User ID to get products for", required = true, example = "1") @PathVariable Long userId) {
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