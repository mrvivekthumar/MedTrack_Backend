package com.medtrack.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteHealthProduct(@PathVariable Long id) {
        return ResponseEntity.ok(healthProductService.deleteHealthProduct(id));
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
}
