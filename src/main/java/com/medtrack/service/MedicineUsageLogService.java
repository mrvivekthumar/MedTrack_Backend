package com.medtrack.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    @Transactional
    public void add(MedicineUsageLogDto logDto) {

        var user = userRepo.findById(logDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        var healthProduct = healthProductRepo.findById(logDto.getHealthProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product Not Found"));

        if (logDto.getIsTaken() && (healthProduct.getTotalQuantity() < healthProduct.getDoseQuantity())) {
            throw new AuthException("Insufficient dose quantity available");
        }

        if (logDto.getIsTaken()) {

            float newAvailableQuantity = healthProduct.getAvailableQuantity() - healthProduct.getDoseQuantity();

            if (newAvailableQuantity < 0.0) {
                throw new com.medtrack.exceptions.AuthException("Insufficient quantity available for product");
            }

            healthProductRepo.updateAvailableQuantityById(healthProduct.getId(), newAvailableQuantity);
        }

        MedicineUsageLog medicineUsageLog = MedicineUsageLog.builder()
                .isTaken(logDto.getIsTaken()) 
                .user(user) // Setting the user of the log
                .healthProduct(healthProduct) // Setting the health product involved
                .build(); 

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

                    long takenCount = productLogs.stream().filter(MedicineUsageLog::getIsTaken).count();
                    dto.setTakenCount(takenCount);
                    dto.setMissedCount(productLogs.size() - takenCount);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<MedicineUsageSummaryDto> getOneDay(Long userId) {

        userRepo.findById(userId).orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        ZonedDateTime kolkataTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        LocalDateTime now = kolkataTime.toLocalDateTime();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay(); // Start of the day
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        List<MedicineUsageLog> logs = medicineUsageLogRepo.findAllByUserIdAndCreatedAtBetween(userId, startOfDay,
                endOfDay);

        if (logs.isEmpty()) {
            List<HealthProduct> healthProducts = healthProductRepo.findByUserId(userId);
            return healthProducts.stream()
                    .collect(Collectors.groupingBy(HealthProduct::getId))
                    .values().stream()
                    .map(productList -> {
                        HealthProduct healthProduct = productList.get(0);

                        MedicineUsageSummaryDto dto = new MedicineUsageSummaryDto();
                        dto.setHealthProductId(healthProduct.getId());
                        dto.setHealthProductName(healthProduct.getName());
                        dto.setMissedCount((long) healthProduct.getMedicineReminders().size());
                        dto.setTakenCount(0L);

                        return dto;
                    })
                    .collect(Collectors.toList());
        }

        // Group logs by health product name
        Map<String, List<MedicineUsageLog>> groupedByHealthProduct = logs.stream()
                .collect(Collectors.groupingBy(log -> log.getHealthProduct().getName()));

        List<MedicineUsageSummaryDto> result = groupedByHealthProduct.values().stream()
                .map(productLogs -> {
                    MedicineUsageLog firstLog = productLogs.get(0);
                    HealthProduct healthProduct = firstLog.getHealthProduct();

                    MedicineUsageSummaryDto dto = new MedicineUsageSummaryDto();
                    dto.setHealthProductId(healthProduct.getId());
                    dto.setHealthProductName(healthProduct.getName());

                    long takenCount = productLogs.size();
                    dto.setTakenCount(takenCount);
                    dto.setMissedCount(healthProduct.getMedicineReminders().size() - takenCount);

                    return dto;
                })
                .collect(Collectors.toList());

        return result;
    }
}
