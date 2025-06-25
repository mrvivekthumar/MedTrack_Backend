package com.medtrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Comprehensive user statistics and adherence metrics")
public record UserStatsDto(
                @Schema(description = "Total number of medicines registered by the user", example = "5") Integer totalMedicines,

                @Schema(description = "Number of active medicines (not expired, quantity > 0)", example = "3") Integer activeMedicines,

                @Schema(description = "Number of expired medicines", example = "1") Integer expiredMedicines,

                @Schema(description = "Number of medicines with low stock (below threshold)", example = "2") Integer lowStockMedicines,

                @Schema(description = "Medication adherence rate as percentage (0-100)", example = "85.5") Double adherenceRate,

                @Schema(description = "Total number of doses taken in the last 7 days", example = "42") Long totalDosesTaken,

                @Schema(description = "Total number of doses missed in the last 7 days", example = "8") Long totalDosesMissed) {
}
