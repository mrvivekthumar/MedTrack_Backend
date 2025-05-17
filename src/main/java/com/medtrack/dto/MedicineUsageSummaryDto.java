package com.medtrack.dto;

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
public class MedicineUsageSummaryDto {
    private Long healthProductId;
    private String healthProductName;
    private Long takenCount;
    private Long missedCount;
}