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

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/health-product")
public class HealthProductController {

    private final HealthProductService healthProductService;
    private final HealthProductMapper healthProductMapper;

    @PostMapping("/createHealthProduct")
    public ResponseEntity<HealthProductResponseDto> createHealthProduct(
            @Valid @RequestBody HealthProductRequestDto healthProductRequestDto) {
        HealthProduct savedProduct = healthProductService.createHealthProduct(healthProductRequestDto);
        return ResponseEntity.ok(healthProductMapper.toDto(savedProduct));
    }

    @PutMapping("/{healthProductId}")
    public ResponseEntity<HealthProductResponseDto> updateHealthProduct(
            @PathVariable("healthProductId") Long healthProductId,
            @RequestBody HealthProductRequestDto dto) {
        HealthProduct existingProduct = healthProductService.updateHealthProduct(healthProductId, dto);
        return ResponseEntity.ok(healthProductMapper.toDto(existingProduct));
    }

    @DeleteMapping("/{healthProductId}")
    public ResponseEntity<String> deleteHealthProduct(@PathVariable("healthProductId") Long healthProductId) {
        healthProductService.deleteHealthProduct(healthProductId);
        return ResponseEntity.ok("Health product with ID %d is deleted".formatted(healthProductId));
    }

    @GetMapping("/{healthProductId}")
    public ResponseEntity<HealthProductResponseDto> getHealthProduct(
            @PathVariable("healthProductId") Long healthProductId) {
        HealthProduct healthProduct = healthProductService.getHealthProduct(healthProductId);
        return ResponseEntity.ok(healthProductMapper.toDto(healthProduct));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<HealthProductResponseDto>> getActiveHealthProducts(@PathVariable("userId") Long userId) {
        List<HealthProduct> activeHealthProducts = healthProductService.getActiveHealthProducts(userId);
        return ResponseEntity.ok(healthProductMapper.toDtoList(activeHealthProducts));
    }

    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<HealthProductResponseDto>> getAllHealthProducts(@PathVariable("userId") Long userId) {
        List<HealthProduct> allHealthProduct = healthProductService.getAllHealthProducts(userId);
        return ResponseEntity.ok(healthProductMapper.toDtoList(allHealthProduct));
    }

    @GetMapping("/user/{userId}/low-stock")

    public ResponseEntity<List<HealthProductResponseDto>> getLowStockHealthProducts(
            @PathVariable("userId") Long userId) {
        List<HealthProduct> lowStockHealthProducts = healthProductService.getLowStockHealthProducts(userId);
        return ResponseEntity.ok(healthProductMapper.toDtoList(lowStockHealthProducts));
    }

    @PostMapping("/{healthProductId}/record-usage")
    public ResponseEntity<HealthProductResponseDto> recordMedicineUsage(
            @PathVariable("healthProductId") Long healthProductId) {
        HealthProduct updatedProduct = healthProductService.recordMedicineUsage(healthProductId);
        return ResponseEntity.ok(healthProductMapper.toDto(updatedProduct));
    }
}
