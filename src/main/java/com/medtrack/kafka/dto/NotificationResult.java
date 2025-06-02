package com.medtrack.kafka.dto;

import java.time.LocalDateTime;

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
public class NotificationResult {

    private String messageId;
    private String correlationId;
    private boolean success;
    private String errorMessage;
    private LocalDateTime processedAt;
    private String processingNode;
    private Long processingTimeMs;
}