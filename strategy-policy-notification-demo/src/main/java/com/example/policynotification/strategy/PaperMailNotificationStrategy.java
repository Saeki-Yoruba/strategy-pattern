package com.example.policynotification.strategy;

import com.example.policynotification.exception.InvalidNotificationDestinationException;
import com.example.policynotification.gateway.PrintTaskGateway;
import com.example.policynotification.model.NotificationResult;
import com.example.policynotification.model.NotificationType;
import com.example.policynotification.model.PolicyRenewalNotificationData;
import com.example.policynotification.support.PersonalDataMasker;
import org.springframework.stereotype.Component;

@Component
public class PaperMailNotificationStrategy implements NotificationStrategy {

    private final PrintTaskGateway gateway;

    public PaperMailNotificationStrategy(PrintTaskGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public NotificationType supports() {
        return NotificationType.PAPER_MAIL;
    }

    @Override
    public NotificationResult send(PolicyRenewalNotificationData data) {
        if (data.postalAddress() == null || data.postalAddress().isBlank()) {
            throw new InvalidNotificationDestinationException("郵寄地址不存在");
        }
        gateway.create(data.postalAddress(), "保單%s續期通知".formatted(data.policyNumber()));
        String suffix = PersonalDataMasker.policySuffix(data.policyNumber());
        return StrategyResultFactory.success(
                data,
                PersonalDataMasker.address(data.postalAddress()),
                "保單末四碼 %s 的紙本通知列印任務已建立".formatted(suffix)
        );
    }
}
