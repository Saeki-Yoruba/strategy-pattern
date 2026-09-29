package com.example.policynotification.strategy;

import com.example.policynotification.exception.InvalidNotificationDestinationException;
import com.example.policynotification.gateway.SmsGateway;
import com.example.policynotification.model.NotificationResult;
import com.example.policynotification.model.NotificationType;
import com.example.policynotification.model.PolicyRenewalNotificationData;
import com.example.policynotification.support.PersonalDataMasker;
import org.springframework.stereotype.Component;

@Component
public class SmsNotificationStrategy implements NotificationStrategy {

    private final SmsGateway gateway;

    public SmsNotificationStrategy(SmsGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public NotificationType supports() {
        return NotificationType.SMS;
    }

    @Override
    public NotificationResult send(PolicyRenewalNotificationData data) {
        if (data.mobileNumber() == null || !data.mobileNumber().matches("09\\d{8}")) {
            throw new InvalidNotificationDestinationException("手機號碼不存在或格式錯誤");
        }
        // 簡訊可能顯示於手機鎖定畫面，因此不顯示完整保單號碼。
        String suffix = PersonalDataMasker.policySuffix(data.policyNumber());
        String content = "保單末四碼%s將於%s到期，應繳保費%,.0f元。".formatted(
                suffix, data.paymentDueDate(), data.premiumAmount());
        gateway.send(data.mobileNumber(), content);
        return StrategyResultFactory.success(
                data,
                PersonalDataMasker.mobile(data.mobileNumber()),
                "保單末四碼 %s 的續期簡訊已送出".formatted(suffix)
        );
    }
}
