package com.medtrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Medicine usage log entry details")
public class MedicineUsageLogDto {

    @Schema(description = "ID of the user logging the medicine usage", example = "1", required = true)
    @NotNull(message = "User ID is required")
    private Long userId;

    @Schema(description = "ID of the health product being logged", example = "1", required = true)
    @NotNull(message = "Health product ID is required")
    private Long healthProductId;

    @Schema(description = "Whether the medicine was taken (true) or missed (false)", example = "true", required = true)
    @NotNull(message = "isTaken status is required")
    private Boolean isTaken;
}
