package com.medtrack.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medtrack.dto.MedicineUsageLogDto;
import com.medtrack.dto.MedicineUsageSummaryDto;
import com.medtrack.service.MedicineUsageLogService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/medicine-logs")
public class MedicineUsageLogController {

    private final MedicineUsageLogService medicineUsageLogService;

    @GetMapping("/{userId}/time/{days}")
    public ResponseEntity<List<MedicineUsageSummaryDto>> getLogsForPastDays(@PathVariable("userId") Long userId,
            @PathVariable("days") Integer days) {
        return ResponseEntity.ok(medicineUsageLogService.getLogForTime(userId, days));
    }

    @PostMapping("/log")
    public ResponseEntity<Void> addLog(@RequestBody MedicineUsageLogDto logDto) {
        System.out.println("Received log: " + logDto);
        medicineUsageLogService.add(logDto);
        // return ResponseEntity.ok().build();
        System.out.println("Log added successfully");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{userId}/today")
    public ResponseEntity<List<MedicineUsageSummaryDto>> getTodayLogs(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(medicineUsageLogService.getOneDay(userId));
    }
}
