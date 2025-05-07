package com.medtrack.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.medtrack.model.HealthProduct;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
class ExpiryNotificationTask {
    private LocalDate expiryDate;
    private Long productId;
    private String productName;
    private boolean reminderSent;
}

/**
 * Service responsible for managing medicine expiry notifications.
 * Maintains a priority queue of products ordered by expiry date
 * and sends notifications when products are about to expire.
 */
@Component
@Scope("singleton")
public class MedicineExpiryNotificationService implements Runnable {

    private final MailSenderService mailSender;
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Kolkata");
    private static final int NOTIFICATION_DAYS_BEFORE = 2;

    private static final PriorityQueue<ExpiryNotificationTask> notificationQueue = new PriorityQueue<>(
            (task1, task2) -> task1.getExpiryDate().compareTo(task2.getExpiryDate()));

    private static final Lock queueLock = new ReentrantLock();
    private static Thread notificationThread;
    private volatile boolean isRunning = true;

    public MedicineExpiryNotificationService(MailSenderService mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Adds a health product to the expiry notification queue
     * 
     * @param product The health product to track for expiry
     * @return true if product was added to queue, false if already expired
     */
    public boolean scheduleMedicineExpiryNotification(HealthProduct product) {
        LocalDate currentDate = getCurrentLocalDate();
        ExpiryNotificationTask task = new ExpiryNotificationTask(
                product.getExpiryDate(),
                product.getId(),
                product.getName(), // Assuming HealthProduct has a getName() method
                false);

        // Don't add already expired products
        if (currentDate.isAfter(task.getExpiryDate())) {
            return false;
        }

        queueLock.lock();
        try {
            // First item - start the thread
            if (notificationQueue.isEmpty()) {
                notificationQueue.add(task);
                startNotificationThread();
                return true;
            }

            // New task expires sooner than current earliest - restart thread
            ExpiryNotificationTask earliestTask = notificationQueue.peek();
            notificationQueue.add(task);

            if (task.getExpiryDate().isBefore(earliestTask.getExpiryDate())) {
                restartNotificationThread();
            }

            return true;
        } finally {
            queueLock.unlock();
        }
    }

    /**
     * Updates an existing product in the notification queue
     * 
     * @param product The updated health product
     * @return true if product was updated, false if not found
     */
    public boolean updateMedicineExpiryNotification(HealthProduct product) {
        queueLock.lock();
        try {
            // Remove the existing entry
            boolean removed = removeMedicineExpiryNotification(product.getId());
            if (!removed) {
                return false;
            }

            // Add the updated product
            return scheduleMedicineExpiryNotification(product);
        } finally {
            queueLock.unlock();
        }
    }

    /**
     * Removes a product from the notification queue by its ID
     * 
     * @param productId The ID of the product to remove
     * @return true if product was found and removed, false otherwise
     */
    public boolean removeMedicineExpiryNotification(Long productId) {
        queueLock.lock();
        try {
            int initialSize = notificationQueue.size();
            notificationQueue.removeIf(task -> task.getProductId().equals(productId));
            return notificationQueue.size() < initialSize;
        } finally {
            queueLock.unlock();
        }
    }

    /**
     * Retrieves all products currently in the notification queue
     * 
     * @return List of all products scheduled for expiry notification
     */
    public List<ExpiryNotificationTask> getAllScheduledNotifications() {
        queueLock.lock();
        try {
            return new ArrayList<>(notificationQueue);
        } finally {
            queueLock.unlock();
        }
    }

    /**
     * Finds a specific product in the notification queue by ID
     * 
     * @param productId The ID of the product to find
     * @return Optional containing the task if found, empty otherwise
     */
    public Optional<ExpiryNotificationTask> findNotificationById(Long productId) {
        queueLock.lock();
        try {
            return notificationQueue.stream()
                    .filter(task -> task.getProductId().equals(productId))
                    .findFirst();
        } finally {
            queueLock.unlock();
        }
    }

    /**
     * Starts the notification thread if it's not already running
     */
    private void startNotificationThread() {
        if (notificationThread == null || !notificationThread.isAlive()) {
            notificationThread = new Thread(this);
            notificationThread.setName("MedicineExpiryNotifier");
            notificationThread.setDaemon(true);
            notificationThread.start();
        }
    }

    /**
     * Restarts the notification thread to recalculate timing
     */
    private void restartNotificationThread() {
        if (notificationThread != null && notificationThread.isAlive()) {
            notificationThread.interrupt();
        }
    }

    /**
     * Gets the current local date in the configured time zone
     */
    private LocalDate getCurrentLocalDate() {
        return ZonedDateTime.now(ZONE_ID).toLocalDate();
    }

    /**
     * Stops the notification service gracefully
     */
    public void shutdown() {
        isRunning = false;
        if (notificationThread != null) {
            notificationThread.interrupt();
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            ExpiryNotificationTask nextTask = null;

            queueLock.lock();
            try {
                if (notificationQueue.isEmpty()) {
                    break;
                }
                nextTask = notificationQueue.peek();
            } finally {
                queueLock.unlock();
            }

            if (nextTask == null) {
                break;
            }

            try {
                processNextNotification(nextTask);
            } catch (InterruptedException e) {
                // Thread was interrupted, likely because a new task with earlier expiry was
                // added
                Thread.currentThread().interrupt();
                continue;
            } catch (Exception e) {
                // Log error but continue processing other notifications
                System.err.println("Error processing notification for product ID: " +
                        nextTask.getProductId() + ": " + e.getMessage());
                e.printStackTrace();

                // Remove the problematic task and continue
                queueLock.lock();
                try {
                    notificationQueue.poll();
                } finally {
                    queueLock.unlock();
                }
            }
        }
    }

    /**
     * Processes the next notification in the queue
     */
    private void processNextNotification(ExpiryNotificationTask task) throws InterruptedException {
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);
        LocalDate notificationDate = task.getExpiryDate().minusDays(NOTIFICATION_DAYS_BEFORE);
        ZonedDateTime notificationTime = notificationDate.atStartOfDay(ZONE_ID);

        if (now.isAfter(notificationTime)) {
            // Already past notification time, send immediately
            sendNotificationAndRemove(task);
        } else {
            // Calculate sleep time - but limit to avoid long Thread.sleep
            long daysToWait = ChronoUnit.DAYS.between(now.toLocalDate(), notificationDate);

            if (daysToWait <= 7) {
                // For nearby dates, use millisecond precision
                long millisToSleep = ChronoUnit.MILLIS.between(now, notificationTime);
                if (millisToSleep > 0) {
                    Thread.sleep(millisToSleep);
                }
                sendNotificationAndRemove(task);
            } else {
                // For distant dates, sleep 1 day at a time to allow for queue updates
                Thread.sleep(24 * 60 * 60 * 1000); // 1 day
            }
        }
    }

    /**
     * Sends the notification email and removes the task from the queue
     */
    private void sendNotificationAndRemove(ExpiryNotificationTask task) {
        try {
            mailSender.sendExpiryNotification(task.getProductId(), task.getProductName(), task.getExpiryDate());

            queueLock.lock();
            try {
                notificationQueue.poll(); // Remove the processed task
            } finally {
                queueLock.unlock();
            }
        } catch (Exception e) {
            System.err.println("Failed to send notification for product ID: " + task.getProductId());
            e.printStackTrace();
        }
    }
}