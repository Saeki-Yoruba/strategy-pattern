package com.example.policynotification.strategy;

import com.example.policynotification.model.NotificationResult;
import com.example.policynotification.model.NotificationStatus;
import com.example.policynotification.model.PolicyRenewalNotificationData;
import com.example.policynotification.support.PersonalDataMasker;

import java.time.LocalDateTime;

final class StrategyResultFactory {

    private StrategyResultFactory() {
    }

    static NotificationResult success(
            PolicyRenewalNotificationData data,
            String maskedDestination,
            String message
    ) {
        return new NotificationResult(
                data.policyNumber(),
                PersonalDataMasker.customerId(data.customerId()),
                data.notificationType(),
                NotificationStatus.SUCCESS,
                maskedDestination,
                message,
                LocalDateTime.now()
        );
    }
}
