package com.medtrack.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medtrack.dto.HealthProductRequestDto;
import com.medtrack.dto.HealthProductResponseDto;
import com.medtrack.mapper.HealthProductMapper;
import com.medtrack.model.HealthProduct;
import com.medtrack.service.HealthProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/health-product")
@Tag(name = "Health Product Management", description = "Operations for managing medicines and health products including inventory, reminders, and usage tracking")
@SecurityRequirement(name = "bearerAuth")
public class HealthProductController {

    private final HealthProductService healthProductService;
    private final HealthProductMapper healthProductMapper;

    @PostMapping("/createHealthProduct")
    @Operation(summary = "Create a new health product", description = """
            Creates a new medicine/health product with the following features:
            - Automatic inventory tracking
            - Expiry date monitoring
            - Medicine reminder scheduling
            - Low stock threshold alerts
            - Integration with notification system
            """, tags = { "Product Management" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Health product created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HealthProductResponseDto.class), examples = @ExampleObject(value = """
                    {
                        "healthProductId": 1,
                        "healthProductName": "Paracetamol 500mg",
                        "totalQuantity": 100.0,
                        "availableQuantity": 100.0,
                        "thresholdQuantity": 10.0,
                        "doseQuantity": 1.0,
                        "unit": "tablets",
                        "expiryDate": "2025-12-31",
                        "reminderTimes": ["08:00", "20:00"],
                        "createdAt": "2024-01-15T10:30:00"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or validation errors", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "errors": {
                            "healthProductName": "Product name is required",
                            "totalQuantity": "Total quantity must be greater than 0"
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<HealthProductResponseDto> createHealthProduct(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Health product creation details", required = true, content = @Content(schema = @Schema(implementation = HealthProductRequestDto.class), examples = @ExampleObject(value = """
                    {
                        "userId": 1,
                        "healthProductName": "Paracetamol 500mg",
                        "totalQuantity": 100.0,
                        "thresholdQuantity": 10.0,
                        "doseQuantity": 1.0,
                        "unit": "tablets",
                        "expiryDate": "2025-12-31",
                        "reminderTimes": ["08:00", "20:00"]
                    }
                    """))) @Valid @RequestBody HealthProductRequestDto healthProductRequestDto) {
        HealthProduct savedProduct = healthProductService.createHealthProduct(healthProductRequestDto);
        return ResponseEntity.ok(healthProductMapper.toDto(savedProduct));
    }

    @PutMapping("/{healthProductId}")
    @Operation(summary = "Update health product", description = """
            Updates an existing health product. All fields are optional, only provided fields will be updated.
            - Updates inventory quantities
            - Modifies reminder schedules
            - Changes expiry dates and thresholds
            - Automatically updates notification schedules
            """, tags = { "Product Management" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Health product updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HealthProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Health product not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<HealthProductResponseDto> updateHealthProduct(
            @Parameter(description = "Health product ID to update", required = true, example = "1") @PathVariable("healthProductId") Long healthProductId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated health product information", required = true, content = @Content(schema = @Schema(implementation = HealthProductRequestDto.class), examples = @ExampleObject(value = """
                    {
                        "healthProductName": "Paracetamol 500mg Updated",
                        "totalQuantity": 120.0,
                        "availableQuantity": 95.0,
                        "thresholdQuantity": 15.0,
                        "doseQuantity": 1.0,
                        "unit": "tablets",
                        "expiryDate": "2026-01-31",
                        "reminderTimes": ["08:00", "14:00", "20:00"]
                    }
                    """))) @RequestBody HealthProductRequestDto dto) {
        HealthProduct existingProduct = healthProductService.updateHealthProduct(healthProductId, dto);
        return ResponseEntity.ok(healthProductMapper.toDto(existingProduct));
    }

    @DeleteMapping("/{healthProductId}")
    @Operation(summary = "Delete health product", description = """
            Permanently deletes a health product and all associated data including:
            - Medicine reminders
            - Usage logs
            - Notification schedules

            ⚠️ **Warning**: This action cannot be undone!
            """, tags = { "Product Management" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Health product deleted successfully", content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Health product with ID 1 is deleted"))),
            @ApiResponse(responseCode = "404", description = "Health product not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<String> deleteHealthProduct(
            @Parameter(description = "Health product ID to delete", required = true, example = "1") @PathVariable("healthProductId") Long healthProductId) {
        healthProductService.deleteHealthProduct(healthProductId);
        return ResponseEntity.ok("Health product with ID %d is deleted".formatted(healthProductId));
    }

    @GetMapping("/{healthProductId}")
    @Operation(summary = "Get health product details", description = "Retrieves detailed information about a specific health product including current inventory and reminder settings", tags = {
            "Product Information" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Health product details retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HealthProductResponseDto.class), examples = @ExampleObject(value = """
                    {
                        "healthProductId": 1,
                        "healthProductName": "Paracetamol 500mg",
                        "totalQuantity": 100.0,
                        "availableQuantity": 75.0,
                        "thresholdQuantity": 10.0,
                        "doseQuantity": 1.0,
                        "unit": "tablets",
                        "expiryDate": "2025-12-31",
                        "reminderTimes": ["08:00", "20:00"],
                        "createdAt": "2024-01-15T10:30:00"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Health product not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<HealthProductResponseDto> getHealthProduct(
            @Parameter(description = "Health product ID", required = true, example = "1") @PathVariable("healthProductId") Long healthProductId) {
        HealthProduct healthProduct = healthProductService.getHealthProduct(healthProductId);
        return ResponseEntity.ok(healthProductMapper.toDto(healthProduct));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get active health products", description = """
            Retrieves all active health products for a user. Active products are those that:
            - Have available quantity > 0
            - Are not expired (expiry date is in the future)
            - Are currently being tracked
            """, tags = { "Product Information" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active health products retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = HealthProductResponseDto.class)), examples = @ExampleObject(value = """
                    [
                        {
                            "healthProductId": 1,
                            "healthProductName": "Paracetamol 500mg",
                            "totalQuantity": 100.0,
                            "availableQuantity": 75.0,
                            "thresholdQuantity": 10.0,
                            "doseQuantity": 1.0,
                            "unit": "tablets",
                            "expiryDate": "2025-12-31",
                            "reminderTimes": ["08:00", "20:00"],
                            "createdAt": "2024-01-15T10:30:00"
                        },
                        {
                            "healthProductId": 2,
                            "healthProductName": "Vitamin D3",
                            "totalQuantity": 60.0,
                            "availableQuantity": 45.0,
                            "thresholdQuantity": 5.0,
                            "doseQuantity": 1.0,
                            "unit": "capsules",
                            "expiryDate": "2025-06-15",
                            "reminderTimes": ["09:00"],
                            "createdAt": "2024-01-10T14:20:00"
                        }
                    ]
                    """))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<HealthProductResponseDto>> getActiveHealthProducts(
            @Parameter(description = "User ID to get active products for", required = true, example = "1") @PathVariable("userId") Long userId) {
        List<HealthProduct> activeHealthProducts = healthProductService.getActiveHealthProducts(userId);
        return ResponseEntity.ok(healthProductMapper.toDtoList(activeHealthProducts));
    }

    @GetMapping("/user/{userId}/all")
    @Operation(summary = "Get all health products", description = """
            Retrieves ALL health products for a user, including:
            - Active products
            - Expired products
            - Out-of-stock products
            - Discontinued products

            This is useful for complete inventory management and historical tracking.
            """, tags = { "Product Information" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All health products retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = HealthProductResponseDto.class)))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<HealthProductResponseDto>> getAllHealthProducts(
            @Parameter(description = "User ID to get all products for", required = true, example = "1") @PathVariable("userId") Long userId) {
        List<HealthProduct> allHealthProduct = healthProductService.getAllHealthProducts(userId);
        return ResponseEntity.ok(healthProductMapper.toDtoList(allHealthProduct));
    }

    @GetMapping("/user/{userId}/low-stock")
    @Operation(summary = "Get low stock health products", description = """
            Retrieves health products that are running low on stock. Products are considered low stock when:
            - Available quantity ≤ Threshold quantity
            - Product is not expired
            - Total quantity > 0

            🚨 **Auto-notification**: This endpoint also triggers low stock notifications via Kafka.
            """, tags = { "Inventory Management" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Low stock products retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = HealthProductResponseDto.class)), examples = @ExampleObject(value = """
                    [
                        {
                            "healthProductId": 3,
                            "healthProductName": "Aspirin 75mg",
                            "totalQuantity": 50.0,
                            "availableQuantity": 5.0,
                            "thresholdQuantity": 10.0,
                            "doseQuantity": 1.0,
                            "unit": "tablets",
                            "expiryDate": "2025-08-20",
                            "reminderTimes": ["07:00"],
                            "createdAt": "2024-01-05T09:15:00"
                        }
                    ]
                    """))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<HealthProductResponseDto>> getLowStockHealthProducts(
            @Parameter(description = "User ID to check for low stock products", required = true, example = "1") @PathVariable("userId") Long userId) {
        List<HealthProduct> lowStockHealthProducts = healthProductService.getLowStockHealthProducts(userId);
        return ResponseEntity.ok(healthProductMapper.toDtoList(lowStockHealthProducts));
    }

    @PostMapping("/{healthProductId}/record-usage")
    @Operation(summary = "Record medicine usage", description = """
            Records that a dose of medicine has been taken, which:
            - Reduces available quantity by the dose amount
            - Checks for low stock or out-of-stock conditions
            - Triggers appropriate notifications via Kafka
            - Updates inventory automatically

            📊 **Smart Monitoring**: Automatically sends alerts when stock gets low or runs out.
            """, tags = { "Usage Tracking" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Medicine usage recorded successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HealthProductResponseDto.class), examples = @ExampleObject(value = """
                    {
                        "healthProductId": 1,
                        "healthProductName": "Paracetamol 500mg",
                        "totalQuantity": 100.0,
                        "availableQuantity": 74.0,
                        "thresholdQuantity": 10.0,
                        "doseQuantity": 1.0,
                        "unit": "tablets",
                        "expiryDate": "2025-12-31",
                        "reminderTimes": ["08:00", "20:00"],
                        "createdAt": "2024-01-15T10:30:00"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Insufficient quantity available for dose", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "errors": {
                            "error": "Insufficient quantity available for dose"
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Health product not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<HealthProductResponseDto> recordMedicineUsage(
            @Parameter(description = "Health product ID to record usage for", required = true, example = "1") @PathVariable("healthProductId") Long healthProductId) {
        HealthProduct updatedProduct = healthProductService.recordMedicineUsage(healthProductId);
        return ResponseEntity.ok(healthProductMapper.toDto(updatedProduct));
    }
}