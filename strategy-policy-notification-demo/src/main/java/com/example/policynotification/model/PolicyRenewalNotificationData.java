package com.example.policynotification.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Provider 準備完成的內部資料。Strategy 只專注通知，不自行查資料，
 * 可避免每個策略重複實作資料存取，也讓策略單元測試保持單純。
 */
public record PolicyRenewalNotificationData(
        String policyNumber,
        String customerId,
        String customerName,
        LocalDate paymentDueDate,
        BigDecimal premiumAmount,
        NotificationType notificationType,
        String email,
        String mobileNumber,
        String postalAddress,
        String appDeviceToken
) {
}
