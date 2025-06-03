package com.medtrack.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.medtrack.dto.MedicineUsageLogDto;
import com.medtrack.dto.MedicineUsageSummaryDto;
import com.medtrack.exceptions.AuthException;
import com.medtrack.model.HealthProduct;
import com.medtrack.model.MedicineUsageLog;
import com.medtrack.repository.HealthProductRepo;
import com.medtrack.repository.MedicineUsageLogRepo;
import com.medtrack.repository.UserRepo;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MedicineUsageLogService {

        private final MedicineUsageLogRepo medicineUsageLogRepo;
        private final UserRepo userRepo;
        private final HealthProductRepo healthProductRepo;
        private final HealthProductService healthProductService;

        @Transactional
        public void add(MedicineUsageLogDto logDto) {

                var user = userRepo.findById(logDto.getUserId())
                                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));
                System.out.println("User found: " + user);
                System.out.println("Log DTO: " + logDto.getHealthProductId());
                var healthProduct = healthProductRepo.findById(logDto.getHealthProductId())
                                .orElseThrow(() -> new EntityNotFoundException("Product Not Found"));
                System.out.println("Health product found: " + healthProduct);

                if (logDto.getIsTaken() && (healthProduct.getTotalQuantity() < healthProduct.getDoseQuantity())) {
                        throw new AuthException("Insufficient dose quantity available");
                }

                System.out.println("Is taken: " + logDto.getIsTaken());

                // if (logDto.getIsTaken()) {
                //         healthProductService.recordMedicineUsage(healthProduct.getId());
                // }

                MedicineUsageLog medicineUsageLog = MedicineUsageLog.builder()
                                .isTaken(logDto.getIsTaken())
                                .user(user) // Setting the user of the log
                                .healthProduct(healthProduct) // Setting the health product involved
                                .build();

                System.out.println("Medicine usage log created: " + medicineUsageLog);
                medicineUsageLogRepo.save(medicineUsageLog);
        }

        public List<MedicineUsageSummaryDto> getLogForTime(Long userId, Integer days) {

                userRepo.findById(userId).orElseThrow(() -> new EntityNotFoundException("User Not Found"));

                ZonedDateTime kolkataTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
                ZonedDateTime dateFromKolkata = kolkataTime.minusDays(days);
                LocalDateTime dateFrom = dateFromKolkata.toLocalDateTime();

                List<MedicineUsageLog> logs = medicineUsageLogRepo.findAllByUserIdAndCreatedAtIsAfter(userId, dateFrom);

                Map<Long, List<MedicineUsageLog>> groupedByHealthProduct = logs.stream()
                                .collect(Collectors.groupingBy(product -> product.getHealthProduct().getId()));

                // Convert to DTOs
                return groupedByHealthProduct.entrySet().stream()
                                .map(entry -> {
                                        Long productId = entry.getKey();
                                        List<MedicineUsageLog> productLogs = entry.getValue();

                                        MedicineUsageSummaryDto dto = new MedicineUsageSummaryDto();
                                        dto.setHealthProductId(productId);
                                        dto.setHealthProductName(productLogs.get(0).getHealthProduct().getName());

                                        long takenCount = productLogs.stream().filter(MedicineUsageLog::getIsTaken)
                                                        .count();
                                        dto.setTakenCount(takenCount);
                                        dto.setMissedCount(productLogs.size() - takenCount);

                                        return dto;
                                })
                                .collect(Collectors.toList());
        }

        public List<MedicineUsageSummaryDto> getOneDay(Long userId) {
                // Verify user exists
                userRepo.findById(userId).orElseThrow(() -> new EntityNotFoundException("User Not Found"));

                // Get today's date range
                ZonedDateTime kolkataTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
                LocalDateTime now = kolkataTime.toLocalDateTime();
                LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
                LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

                // Get today's logs
                List<MedicineUsageLog> logs = medicineUsageLogRepo.findAllByUserIdAndCreatedAtBetween(userId,
                                startOfDay,
                                endOfDay);

                // 🔧 CASE 1: No logs for today - return default data
                if (logs.isEmpty()) {
                        List<HealthProduct> healthProducts = healthProductRepo.findByUserId(userId);
                        return healthProducts.stream()
                                        .filter(healthProduct -> healthProduct != null) // Filter null products
                                        .collect(Collectors.groupingBy(HealthProduct::getId))
                                        .values().stream()
                                        .map(productList -> {
                                                HealthProduct healthProduct = productList.get(0);

                                                MedicineUsageSummaryDto dto = new MedicineUsageSummaryDto();
                                                dto.setHealthProductId(healthProduct.getId());
                                                dto.setHealthProductName(
                                                                healthProduct.getName() != null
                                                                                ? healthProduct.getName()
                                                                                : "Unknown Medicine");

                                                // Safe null check for medicineReminders
                                                long reminderCount = healthProduct.getMedicineReminders() != null
                                                                ? healthProduct.getMedicineReminders().size()
                                                                : 0;
                                                dto.setMissedCount(reminderCount);
                                                dto.setTakenCount(0L);

                                                return dto;
                                        })
                                        .collect(Collectors.toList());
                }

                // 🔧 CASE 2: We have logs - process them safely

                // First, filter out any logs with null health products or names
                List<MedicineUsageLog> validLogs = logs.stream()
                                .filter(log -> log != null)
                                .filter(log -> log.getHealthProduct() != null)
                                .filter(log -> log.getHealthProduct().getName() != null)
                                .collect(Collectors.toList());

                // If all logs are invalid, return empty list
                if (validLogs.isEmpty()) {
                        System.out.println("⚠️ All logs have null health products or names");
                        return new ArrayList<>();
                }

                // Group valid logs by health product name
                Map<String, List<MedicineUsageLog>> groupedByHealthProduct = validLogs.stream()
                                .collect(Collectors.groupingBy(log -> log.getHealthProduct().getName()));

                // Convert to DTOs
                List<MedicineUsageSummaryDto> result = groupedByHealthProduct.values().stream()
                                .map(productLogs -> {
                                        MedicineUsageLog firstLog = productLogs.get(0);
                                        HealthProduct healthProduct = firstLog.getHealthProduct();

                                        MedicineUsageSummaryDto dto = new MedicineUsageSummaryDto();
                                        dto.setHealthProductId(healthProduct.getId());
                                        dto.setHealthProductName(healthProduct.getName());

                                        long takenCount = productLogs.size();
                                        dto.setTakenCount(takenCount);

                                        // Safe null check for medicineReminders
                                        long reminderCount = healthProduct.getMedicineReminders() != null
                                                        ? healthProduct.getMedicineReminders().size()
                                                        : 0;
                                        dto.setMissedCount(Math.max(0, reminderCount - takenCount)); // Ensure
                                                                                                     // non-negative

                                        return dto;
                                })
                                .collect(Collectors.toList());

                return result;
        }
}