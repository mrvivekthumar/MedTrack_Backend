// Create this file: src/main/java/com/medtrack/kafka/service/NotificationConsumerService.java
package com.medtrack.kafka.service;

import com.medtrack.kafka.dto.NotificationMessage;
import com.medtrack.kafka.dto.NotificationResult;
import com.medtrack.kafka.dto.NotificationType;
import com.medtrack.utils.MailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumerService {

    private final MailSenderService mailSenderService;
    private final KafkaTemplate<String, NotificationResult> resultKafkaTemplate;

    @Value("${medtrack.notification.max-retry-attempts}")
    private int maxRetryAttempts;

    @Value("${medtrack.notification.topics.medicine-expiry}")
    private String expiryTopicName;

    /**
     * Main Kafka listener for processing notification messages
     */
    @KafkaListener(topics = "${medtrack.notification.topics.medicine-expiry}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "notificationKafkaListenerContainerFactory")
    public void processNotification(
            @Payload NotificationMessage message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
            @Header(value = KafkaHeaders.OFFSET, required = false) Long offset,
            Acknowledgment acknowledgment) {

        long startTime = System.currentTimeMillis();

        log.info("Received notification: {} from topic: {}, partition: {}, offset: {}",
                message.getMessageId(), topic,
                partition != null ? partition : "unknown",
                offset != null ? offset : "unknown");

        try {
            // Check if this is a scheduled notification
            if (message.getScheduledAt() != null && LocalDateTime.now().isBefore(message.getScheduledAt())) {
                log.info("Message {} is scheduled for future delivery at: {}. Skipping for now.",
                        message.getMessageId(), message.getScheduledAt());
                acknowledgment.acknowledge();
                return;
            }

            // Validate message
            if (!isValidNotificationMessage(message)) {
                log.warn("Invalid notification message received: {}", message.getMessageId());
                sendNotificationResult(message, false, "Invalid message format", startTime);
                acknowledgment.acknowledge();
                return;
            }

            // Process the notification based on type
            boolean success = processNotificationByType(message);

            // Send result back to Kafka (for monitoring/tracking)
            sendNotificationResult(message, success, null, startTime);

            // Acknowledge the message (mark as processed)
            acknowledgment.acknowledge();

            log.info("Successfully processed notification: {} in {}ms",
                    message.getMessageId(), System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("Error processing notification: {}", message.getMessageId(), e);

            // Handle retry logic
            handleNotificationFailure(message, e, acknowledgment, startTime);
        }
    }

    /**
     * Process notification based on its type
     */
    private boolean processNotificationByType(NotificationMessage message) {
        try {
            switch (message.getType()) {
                case MEDICINE_EXPIRY_WARNING:
                    return sendExpiryEmail(message);

                case LOW_STOCK_ALERT:
                    return sendLowStockEmail(message);

                case OUT_OF_STOCK_ALERT:
                    return sendOutOfStockEmail(message);

                case MEDICINE_TAKEN_REMINDER:
                case MEDICINE_MISSED_ALERT:
                    return sendReminderEmail(message);

                default:
                    log.warn("Unknown notification type: {} for message: {}",
                            message.getType(), message.getMessageId());
                    return false;
            }
        } catch (Exception e) {
            log.error("Failed to process notification type: {} for message: {}",
                    message.getType(), message.getMessageId(), e);
            return false;
        }
    }

    /**
     * Send expiry warning email
     */
    private boolean sendExpiryEmail(NotificationMessage message) {
        try {
            mailSenderService.sendExpiryNotification(
                    message.getHealthProductId(),
                    message.getProductName(),
                    message.getExpiryDate());

            log.info("Expiry email sent successfully for product: {} to user: {}",
                    message.getProductName(), message.getUserEmail());
            return true;

        } catch (Exception e) {
            log.error("Failed to send expiry email for product: {}",
                    message.getProductName(), e);
            return false;
        }
    }

    /**
     * Send low stock alert email
     */
    private boolean sendLowStockEmail(NotificationMessage message) {
        try {
            // You can enhance MailSenderService to have a method for low stock emails
            // For now, we'll use the existing method with additional context
            String enhancedInfo = String.format("LOW STOCK ALERT: %s - %s",
                    message.getProductName(), message.getAdditionalInfo());

            // Create a custom email for low stock (you can enhance this)
            mailSenderService.sendExpiryNotification(
                    message.getHealthProductId(),
                    enhancedInfo,
                    message.getExpiryDate());

            log.info("Low stock email sent successfully for product: {} to user: {}",
                    message.getProductName(), message.getUserEmail());
            return true;

        } catch (Exception e) {
            log.error("Failed to send low stock email for product: {}",
                    message.getProductName(), e);
            return false;
        }
    }

    /**
     * Send out of stock alert email
     */
    private boolean sendOutOfStockEmail(NotificationMessage message) {
        try {
            String enhancedInfo = String.format("OUT OF STOCK: %s - Please reorder immediately!",
                    message.getProductName());

            mailSenderService.sendExpiryNotification(
                    message.getHealthProductId(),
                    enhancedInfo,
                    message.getExpiryDate());

            log.info("Out of stock email sent successfully for product: {} to user: {}",
                    message.getProductName(), message.getUserEmail());
            return true;

        } catch (Exception e) {
            log.error("Failed to send out of stock email for product: {}",
                    message.getProductName(), e);
            return false;
        }
    }

    /**
     * Send reminder email
     */
    private boolean sendReminderEmail(NotificationMessage message) {
        try {
            String enhancedInfo = String.format("REMINDER: %s - %s",
                    message.getProductName(), message.getAdditionalInfo());

            mailSenderService.sendExpiryNotification(
                    message.getHealthProductId(),
                    enhancedInfo,
                    message.getExpiryDate());

            log.info("Reminder email sent successfully for product: {} to user: {}",
                    message.getProductName(), message.getUserEmail());
            return true;

        } catch (Exception e) {
            log.error("Failed to send reminder email for product: {}",
                    message.getProductName(), e);
            return false;
        }
    }

    /**
     * Validate notification message
     */
    private boolean isValidNotificationMessage(NotificationMessage message) {
        return message != null &&
                message.getMessageId() != null &&
                message.getType() != null &&
                message.getHealthProductId() != null &&
                message.getUserEmail() != null &&
                message.getProductName() != null;
    }

    /**
     * Handle notification processing failure
     */
    private void handleNotificationFailure(NotificationMessage message, Exception error,
            Acknowledgment acknowledgment, long startTime) {
        try {
            int currentRetryCount = message.getRetryCount() != null ? message.getRetryCount() : 0;

            if (currentRetryCount < maxRetryAttempts) {
                log.warn("Notification {} failed (attempt {}/{}). Will retry later: {}",
                        message.getMessageId(), currentRetryCount + 1, maxRetryAttempts,
                        error.getMessage());

                // For retry, you could send the message back to a retry topic
                // or use Kafka's built-in retry mechanisms
                // For now, we'll just log and acknowledge

                sendNotificationResult(message, false,
                        String.format("Retry %d/%d: %s", currentRetryCount + 1, maxRetryAttempts, error.getMessage()),
                        startTime);
            } else {
                log.error("Notification {} failed permanently after {} attempts: {}",
                        message.getMessageId(), maxRetryAttempts, error.getMessage());

                sendNotificationResult(message, false,
                        String.format("Permanently failed after %d attempts: %s", maxRetryAttempts, error.getMessage()),
                        startTime);
            }

            // Acknowledge to prevent infinite reprocessing
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error handling notification failure for message: {}",
                    message.getMessageId(), e);
            acknowledgment.acknowledge();
        }
    }

    /**
     * Send notification result back to Kafka for tracking
     */
    private void sendNotificationResult(NotificationMessage originalMessage, boolean success,
            String errorMessage, long startTime) {
        try {
            long processingTime = System.currentTimeMillis() - startTime;

            NotificationResult result = NotificationResult.builder()
                    .messageId(originalMessage.getMessageId())
                    .correlationId(originalMessage.getCorrelationId())
                    .success(success)
                    .errorMessage(errorMessage)
                    .processedAt(LocalDateTime.now())
                    .processingNode(getProcessingNodeId())
                    .processingTimeMs(processingTime)
                    .build();

            // Send to results topic (asynchronously)
            CompletableFuture.runAsync(() -> {
                try {
                    resultKafkaTemplate.send("notification-results",
                            originalMessage.getCorrelationId(), result);
                } catch (Exception e) {
                    log.warn("Failed to send notification result: {}", e.getMessage());
                }
            });

        } catch (Exception e) {
            log.warn("Failed to create notification result: {}", e.getMessage());
        }
    }

    /**
     * Get processing node identifier
     */
    private String getProcessingNodeId() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName() + "-" +
                    Thread.currentThread().getName();
        } catch (Exception e) {
            return "unknown-node";
        }
    }
}