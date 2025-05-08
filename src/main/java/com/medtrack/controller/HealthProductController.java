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

import com.medtrack.dto.HealthProductDto;
import com.medtrack.service.HealthProductService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/health-product")
public class HealthProductController {

    private final HealthProductService healthProductService;

    @PostMapping("/createHealthProduct")
    public ResponseEntity<HealthProductDto> createHealthProduct(@RequestBody HealthProductDto healthProductDto) {
        return ResponseEntity.ok(healthProductService.createHealthProduct(healthProductDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HealthProductDto> updateHealthProduct(
            @PathVariable Long id,
            @RequestBody HealthProductDto dto) {
        return ResponseEntity.ok(healthProductService.updateHealthProduct(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteHealthProduct(@PathVariable Long id) {
        return ResponseEntity.ok(healthProductService.deleteHealthProduct(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HealthProductDto> getHealthProductById(@PathVariable Long id) {
        return ResponseEntity.ok(healthProductService.getHealthProductById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<HealthProductDto>> getActiveHealthProducts(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(healthProductService.getActiveHealthProducts(userId));
    }

    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<HealthProductDto>> getAllHealthProducts(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(healthProductService.getAllHealthProducts(userId));
    }

    @GetMapping("/user/{userId}/low-stock")
    public ResponseEntity<List<HealthProductDto>> getLowStockHealthProducts(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(healthProductService.getLowStockHealthProducts(userId));
    }

    @PostMapping("/{id}/record-usage")
    public ResponseEntity<HealthProductDto> recordMedicineUsage(@PathVariable Long id) {
        return ResponseEntity.ok(healthProductService.recordMedicineUsage(id));
    }
}
