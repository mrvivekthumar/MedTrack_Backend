package com.medtrack.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medtrack.dto.MedicineUsageLogDto;
import com.medtrack.dto.MedicineUsageSummaryDto;
import com.medtrack.service.ProductService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/logs")
public class MedicineUsageLogController {

    private final ProductService productService;

    @GetMapping("/{userId}/time/{days}")
    public ResponseEntity<List<MedicineUsageSummaryDto>> getAllLogInDays(@PathVariable("userId") Long id,
            @PathVariable("days") Integer days) {
        return ResponseEntity.ok(productService.getLogForTime(id, days));
    }

    @PostMapping("/log")
    public ResponseEntity<Void> addLog(@RequestBody MedicineUsageLogDto pd) {
        productService.add(pd);
        return ResponseEntity.ok().build();
    }

    @GetMapping("//{id}")
    public ResponseEntity<List<MedicineUsageSummaryDto>> getOneDayNotificationCount(@PathVariable("id") Long id) {
        return ResponseEntity.ok(productService.getOneDay(id));
    }
}
