package com.example.policynotification.strategy;

import com.example.policynotification.exception.InvalidNotificationDestinationException;
import com.example.policynotification.gateway.AppPushGateway;
import com.example.policynotification.model.NotificationResult;
import com.example.policynotification.model.NotificationType;
import com.example.policynotification.model.PolicyRenewalNotificationData;
import com.example.policynotification.support.PersonalDataMasker;
import org.springframework.stereotype.Component;

@Component
public class AppPushNotificationStrategy implements NotificationStrategy {

    private final AppPushGateway gateway;

    public AppPushNotificationStrategy(AppPushGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public NotificationType supports() {
        return NotificationType.APP_PUSH;
    }

    @Override
    public NotificationResult send(PolicyRenewalNotificationData data) {
        if (data.appDeviceToken() == null || data.appDeviceToken().isBlank()) {
            throw new InvalidNotificationDestinationException("App 裝置識別不存在");
        }
        String suffix = PersonalDataMasker.policySuffix(data.policyNumber());
        gateway.push(data.appDeviceToken(), "保單續期提醒", "保單末四碼%s即將到期。".formatted(suffix));
        return StrategyResultFactory.success(
                data,
                PersonalDataMasker.deviceToken(data.appDeviceToken()),
                "保單末四碼 %s 的 App 推播已送出".formatted(suffix)
        );
    }
}
