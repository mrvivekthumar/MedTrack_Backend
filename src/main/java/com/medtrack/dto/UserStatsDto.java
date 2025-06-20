package com.medtrack.dto;

public record UserStatsDto(
        Integer totalMedicines,
        Integer activeMedicines,
        Integer expiredMedicines,
        Integer lowStockMedicines,
        Double adherenceRate,
        Long totalDosesTaken,
        Long totalDosesMissed) {
}
