package com.medtrack.kafka.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.medtrack.kafka.dto.NotificationMessage;
import com.medtrack.kafka.dto.NotificationType;
import com.medtrack.model.HealthProduct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationProducerService {

    private final KafkaTemplate<String, NotificationMessage> kafkaTemplate;

    @Value("${medtrack.notification.topics.medicine-expiry}")
    private String expiryTopicName;

    @Value("${medtrack.notification.expiry-warning-days}")
    private int expiryWarningDays;

    /**
     * Send medicine expiry notification to Kafka
     */
    public void sendExpiryNotification(HealthProduct product) {
        try {
            String messageId = UUID.randomUUID().toString();
            String correlationId = "expiry-" + product.getId() + "-" + System.currentTimeMillis();

            // ✅ FIX: Handle null values gracefully
            String productName = getSafeProductName(product);
            String userEmail = getSafeUserEmail(product);
            String userName = getSafeUserName(product);

            NotificationMessage message = NotificationMessage.builder()
                    .messageId(messageId)
                    .correlationId(correlationId)
                    .type(NotificationType.MEDICINE_EXPIRY_WARNING)
                    .healthProductId(product.getId())
                    .userId(product.getUser() != null ? product.getUser().getId() : null)
                    .productName(productName)
                    .userEmail(userEmail)
                    .userName(userName)
                    .expiryDate(product.getExpiryDate())
                    .scheduledAt(LocalDateTime.now().plusDays(expiryWarningDays))
                    .createdAt(LocalDateTime.now())
                    .retryCount(0)
                    .availableQuantity(product.getAvailableQuantity())
                    .additionalInfo("Medicine expiring in " + expiryWarningDays + " days")
                    .build();

            sendNotificationMessage(expiryTopicName, correlationId, message);

            log.info("Expiry notification queued for product: {} (ID: {})",
                    productName, product.getId());

        } catch (Exception e) {
            log.error("Failed to send expiry notification for product ID: {}",
                    product.getId(), e);
            throw new RuntimeException("Failed to queue expiry notification", e);
        }
    }

    /**
     * Send low stock notification to Kafka
     */
    public void sendLowStockNotification(HealthProduct product) {
        try {
            String messageId = UUID.randomUUID().toString();
            String correlationId = "lowstock-" + product.getId() + "-" + System.currentTimeMillis();

            // ✅ FIX: Handle null values gracefully
            String productName = getSafeProductName(product);
            String userEmail = getSafeUserEmail(product);
            String userName = getSafeUserName(product);
            String unit = product.getUnit() != null ? product.getUnit() : "units";

            NotificationMessage message = NotificationMessage.builder()
                    .messageId(messageId)
                    .correlationId(correlationId)
                    .type(NotificationType.LOW_STOCK_ALERT)
                    .healthProductId(product.getId())
                    .userId(product.getUser() != null ? product.getUser().getId() : null)
                    .productName(productName)
                    .userEmail(userEmail)
                    .userName(userName)
                    .expiryDate(product.getExpiryDate())
                    .scheduledAt(LocalDateTime.now()) // Send immediately
                    .createdAt(LocalDateTime.now())
                    .retryCount(0)
                    .availableQuantity(product.getAvailableQuantity())
                    .thresholdQuantity(product.getThresholdQuantity())
                    .additionalInfo(String.format("Stock low: %.1f %s remaining (threshold: %.1f %s)",
                            product.getAvailableQuantity(), unit,
                            product.getThresholdQuantity(), unit))
                    .build();

            sendNotificationMessage(expiryTopicName, correlationId, message);

            log.info("Low stock notification queued for product: {} (ID: {})",
                    productName, product.getId());

        } catch (Exception e) {
            log.error("Failed to send low stock notification for product ID: {}",
                    product.getId(), e);
        }
    }

    /**
     * Send out of stock notification to Kafka
     */
    public void sendOutOfStockNotification(HealthProduct product) {
        try {
            String messageId = UUID.randomUUID().toString();
            String correlationId = "outofstock-" + product.getId() + "-" + System.currentTimeMillis();

            // ✅ FIX: Handle null values gracefully
            String productName = getSafeProductName(product);
            String userEmail = getSafeUserEmail(product);
            String userName = getSafeUserName(product);

            NotificationMessage message = NotificationMessage.builder()
                    .messageId(messageId)
                    .correlationId(correlationId)
                    .type(NotificationType.OUT_OF_STOCK_ALERT)
                    .healthProductId(product.getId())
                    .userId(product.getUser() != null ? product.getUser().getId() : null)
                    .productName(productName)
                    .userEmail(userEmail)
                    .userName(userName)
                    .expiryDate(product.getExpiryDate())
                    .scheduledAt(LocalDateTime.now()) // Send immediately
                    .createdAt(LocalDateTime.now())
                    .retryCount(0)
                    .availableQuantity(0f)
                    .thresholdQuantity(product.getThresholdQuantity())
                    .additionalInfo("Medicine is out of stock!")
                    .build();

            sendNotificationMessage(expiryTopicName, correlationId, message);

            log.info("Out of stock notification queued for product: {} (ID: {})",
                    productName, product.getId());

        } catch (Exception e) {
            log.error("Failed to send out of stock notification for product ID: {}",
                    product.getId(), e);
        }
    }

    // ✅ NEW: Helper methods to safely extract values
    private String getSafeProductName(HealthProduct product) {
        if (product == null)
            return "Unknown Product";

        String name = product.getName();
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }

        return "Medicine #" + product.getId();
    }

    private String getSafeUserEmail(HealthProduct product) {
        if (product == null || product.getUser() == null) {
            return "no-email@medtrack.com";
        }

        String email = product.getUser().getEmail();
        if (email != null && !email.trim().isEmpty()) {
            return email.trim();
        }

        return "user" + product.getUser().getId() + "@medtrack.com";
    }

    private String getSafeUserName(HealthProduct product) {
        if (product == null || product.getUser() == null) {
            return "Unknown User";
        }

        String fullName = product.getUser().getFullname();
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName.trim();
        }

        return "User #" + product.getUser().getId();
    }

    /**
     * Generic method to send notification message to Kafka
     */
    private void sendNotificationMessage(String topicName, String key, NotificationMessage message) {
        try {
            CompletableFuture<SendResult<String, NotificationMessage>> future = kafkaTemplate.send(topicName, key,
                    message);

            // Add callback for success/failure handling
            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.debug("Notification sent successfully: [{}] with key: [{}] to partition: [{}] at offset: [{}]",
                            message.getMessageId(),
                            key,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send notification: [{}] with key: [{}] due to: {}",
                            message.getMessageId(), key, exception.getMessage(), exception);
                }
            });

        } catch (Exception e) {
            log.error("Error sending message to Kafka topic: {}", topicName, e);
            throw e;
        }
    }

    /**
     * Health check method to verify Kafka connectivity
     */
    public boolean isKafkaHealthy() {
        try {
            String testMessage = "health-check-" + System.currentTimeMillis();
            kafkaTemplate.send("health-check", testMessage,
                    NotificationMessage.builder()
                            .messageId(testMessage)
                            .type(NotificationType.MEDICINE_EXPIRY_WARNING)
                            .createdAt(LocalDateTime.now())
                            .productName("Health Check Test") // ✅ Ensure not null
                            .userEmail("health@medtrack.com") // ✅ Ensure not null
                            .healthProductId(0L) // ✅ Ensure not null
                            .build());
            return true;
        } catch (Exception e) {
            log.warn("Kafka health check failed: {}", e.getMessage());
            return false;
        }
    }
}