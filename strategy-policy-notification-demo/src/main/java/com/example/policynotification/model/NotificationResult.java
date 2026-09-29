package com.example.policynotification.model;

import java.time.LocalDateTime;

public record NotificationResult(
        String policyNumber,
        String maskedCustomerId,
        NotificationType notificationType,
        NotificationStatus status,
        String maskedDestination,
        String messageSummary,
        LocalDateTime processedAt
) {
}
