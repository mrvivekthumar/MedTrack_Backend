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
public class ProductService {

    private final MedicineUsageLogRepo productRepo;
    private final UserRepo userRepo;
    private final HealthProductRepo healthProductRepo;

    @Transactional
    public void add(MedicineUsageLogDto pd) {
        // ZonedDateTime kolkataTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        var user = userRepo.findById(pd.getUserId()).orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        var hp = healthProductRepo.findById(pd.getHealthProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product Not Found"));

        if (pd.getIsTaken()) {

            if (hp.getTotalQuantity() - hp.getDoseQuantity() < 0.0f) {
                throw new com.medtrack.exceptions.AuthException("doseQuantity is Insefisent");
            }
            healthProductRepo.updateAvailableQuantityById(hp.getId(), hp.getTotalQuantity() - hp.getDoseQuantity());
        }

        var pl = MedicineUsageLog.builder()
                .isTaken(pd.getIsTaken()) // Setting the 'isTaken' field from the pd object
                .user(user) // Setting the user of the log
                .healthProduct(hp) // Setting the health product involved
                .build(); // Building the MedicineUsageLog object

        productRepo.save(pl);
    }

    public List<MedicineUsageSummaryDto> getLogForTime(Long userId, Integer days) {
        userRepo.findById(userId).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        ZonedDateTime kolkataTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

        ZonedDateTime dateFromKolkata = kolkataTime.minusDays(days);
        LocalDateTime dateFrom = dateFromKolkata.toLocalDateTime();

        var res = productRepo.findAllByUserIdAndCreatedAtIsAfter(userId, dateFrom);

        Map<Long, List<MedicineUsageLog>> groupedByHealthProduct = res.stream()
                .collect(Collectors.groupingBy(product -> product.getHealthProduct().getId()));

        return groupedByHealthProduct.entrySet().stream()
                .map(entry -> {
                    Long productId = entry.getKey();
                    List<MedicineUsageLog> logs = entry.getValue();

                    MedicineUsageSummaryDto dto = new MedicineUsageSummaryDto();
                    dto.setHealthProductId(productId);
                    dto.setHealthProductName(logs.get(0).getHealthProduct().getName());

                    long takenCount = logs.stream().filter(MedicineUsageLog::getIsTaken).count();
                    dto.setTakenCount(takenCount);
                    dto.setMissedCount(logs.size() - takenCount);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<MedicineUsageSummaryDto> getOneDay(Long id) {
        userRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        ZonedDateTime kolkataTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        LocalDateTime now = kolkataTime.toLocalDateTime();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay(); // Start of the day
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        var res = productRepo.findAllByUserIdAndCreatedAtBetween(id, startOfDay, endOfDay);

        if (res.isEmpty()) {
            List<HealthProduct> ans = healthProductRepo.findAllByUserId(id);

            Map<Long, List<HealthProduct>> groupedByHealthProduct = ans.stream()
                    .collect(Collectors.groupingBy(HealthProduct::getId));

            return groupedByHealthProduct.values().stream().map(
                    healthProducts -> {
                        var healthProduct = healthProducts.get(0);

                        MedicineUsageSummaryDto dto = new MedicineUsageSummaryDto();
                        dto.setHealthProductId(healthProduct.getId());
                        dto.setHealthProductName(healthProduct.getName());

                        // dto.setMissedCount(healthProduct.getMedicineReminders().size());
                        dto.setTakenCount(0L);
                        return dto;
                    }).toList();
        }

        Map<String, List<MedicineUsageLog>> groupedByHealthProduct = res.stream()
                .collect(Collectors.groupingBy(MedicineUsageLog -> MedicineUsageLog.getHealthProduct().getName()));

        return groupedByHealthProduct.values().stream().map(
                MedicineUsageLogs -> {
                    var value = MedicineUsageLogs.get(0);
                    MedicineUsageSummaryDto dto = new MedicineUsageSummaryDto();
                    dto.setHealthProductId(value.getHealthProduct().getId());
                    dto.setHealthProductName(value.getHealthProduct().getName());

                    long count = MedicineUsageLogs.size();
                    dto.setMissedCount(value.getHealthProduct().getMedicineReminders().size() - count);
                    dto.setTakenCount(count);
                    return dto;
                }).toList();
    }
}
