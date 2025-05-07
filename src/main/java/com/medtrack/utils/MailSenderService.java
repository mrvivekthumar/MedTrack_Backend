package com.medtrack.utils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.medtrack.repository.HealthProductRepo;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;

/**
 * Service responsible for sending HTML email notifications related to
 * medication expiry
 */
@Service
@AllArgsConstructor
public class MailSenderService {

    private final JavaMailSender mailSender;
    private final HealthProductRepo healthProductRepo;

    /**
     * Sends an expiry notification email for a health product
     * (Legacy method - kept for backward compatibility)
     * 
     * @param healthProductId The ID of the health product
     */
    public void SendEmail(Long healthProductId) {
        var healthProduct = healthProductRepo.findById(healthProductId).orElse(null);

        if (healthProduct == null)
            return;

        var user = healthProduct.getUser();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String htmlContent = getExpiryHtmlTemplate(healthProduct.getName(),
                    healthProduct.getExpiryDate().toString());

            helper.setTo(user.getEmail());
            helper.setSubject("Your health product is Expiring!!!!!");
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            System.out.printf("Error while sending email: %s%n", e.getMessage());
        }
    }

    /**
     * Sends an expiry notification email for a health product
     * 
     * @param productId   The ID of the health product
     * @param productName The name of the health product
     * @param expiryDate  The expiry date of the health product
     */
    public void sendExpiryNotification(Long productId, String productName, LocalDate expiryDate) {
        var healthProduct = healthProductRepo.findById(productId).orElse(null);

        if (healthProduct == null) {
            System.out.printf("Cannot send notification: Health product with ID %d not found%n", productId);
            return;
        }

        var user = healthProduct.getUser();
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
            System.out.printf("Cannot send notification: User or email not available for product ID %d%n", productId);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Calculate days until expiry for display in email
            long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
            String expiryInfo = daysUntilExpiry > 0
                    ? String.format("%d days", daysUntilExpiry)
                    : "TODAY";

            String htmlContent = getExpiryHtmlTemplate(productName, expiryInfo);

            helper.setTo(user.getEmail());
            helper.setSubject("Medicine Expiry Alert: " + productName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.printf("Expiry notification sent for product: %d - %s%n",
                    productId, productName);

        } catch (MessagingException e) {
            System.out.printf("Error while sending email for product ID %d: %s%n",
                    productId, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generates the HTML template for the expiry notification email
     * 
     * @param medicineName The name of the medicine
     * @param expiryInfo   The expiry information (e.g., "2 days", "TODAY")
     * @return The formatted HTML content
     */
    private String getExpiryHtmlTemplate(String name, String date) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Medicine Expiry Reminder</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background-color: #f4f4f4;
                            margin: 0;
                            padding: 0;
                        }
                        .email-container {
                            max-width: 600px;
                            margin: 20px auto;
                            background-color: #ffffff;
                            padding: 20px;
                            border-radius: 10px;
                            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
                            text-align: center;
                        }
                        .header {
                            background-color: #28a745;
                            color: white;
                            padding: 15px;
                            font-size: 20px;
                            font-weight: bold;
                            border-radius: 10px 10px 0 0;
                        }
                        .content {
                            padding: 20px;
                            font-size: 16px;
                            color: #333;
                        }
                        .medicine-name {
                            font-size: 22px;
                            font-weight: bold;
                            color: #d9534f;
                        }
                        .expiry-days {
                            font-size: 18px;
                            color: #ff9800;
                            font-weight: bold;
                        }
                        .footer {
                            margin-top: 20px;
                            font-size: 14px;
                            color: #777;
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <div class="header">Medicine Expiry Alert</div>
                        <div class="content">
                            <p>Hello,</p>
                            <p>Your medicine <span class="medicine-name">%s</span> is going to expire in <span class="expiry-days">%s</span>.</p>
                            <p>Please ensure you use it before the expiration date or replace it as needed.</p>
                        </div>
                        <div class="footer">
                            <p>Stay healthy and take care!</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(name, date);
    }
}