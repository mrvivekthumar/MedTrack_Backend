package com.medtrack.kafka.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

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
public class NotificationMessage {

    private String messageId;
    private NotificationType type;
    private Long healthProductId;
    private Long userId;
    private String productName;
    private String userEmail;
    private String userName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private Integer retryCount = 0;
    private String correlationId;

    // Additional fields for different notification types
    private Float availableQuantity;
    private Float thresholdQuantity;
    private String additionalInfo;
}
