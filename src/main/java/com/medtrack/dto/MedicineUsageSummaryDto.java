package com.medtrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Summary of medicine usage statistics for a specific product")
public class MedicineUsageSummaryDto {

    @Schema(description = "Health product identifier", example = "1", required = true)
    private Long healthProductId;

    @Schema(description = "Name of the health product", example = "Paracetamol 500mg", required = true)
    private String healthProductName;

    @Schema(description = "Number of doses taken", example = "14", minimum = "0")
    private Long takenCount;

    @Schema(description = "Number of doses missed", example = "2", minimum = "0")
    private Long missedCount;
}