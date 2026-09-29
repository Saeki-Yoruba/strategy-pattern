package com.example.policynotification.strategy;

import com.example.policynotification.exception.InvalidNotificationDestinationException;
import com.example.policynotification.gateway.EmailGateway;
import com.example.policynotification.model.NotificationResult;
import com.example.policynotification.model.NotificationType;
import com.example.policynotification.model.PolicyRenewalNotificationData;
import com.example.policynotification.support.PersonalDataMasker;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class EmailNotificationStrategy implements NotificationStrategy {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private final EmailGateway gateway;

    public EmailNotificationStrategy(EmailGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public NotificationType supports() {
        return NotificationType.EMAIL;
    }

    @Override
    public NotificationResult send(PolicyRenewalNotificationData data) {
        if (data.email() == null || !EMAIL_PATTERN.matcher(data.email()).matches()) {
            throw new InvalidNotificationDestinationException("Email 不存在或格式錯誤");
        }
        String body = "%s您好，保單%s將於%s到期，本期應繳保費為%,.0f元。".formatted(
                data.customerName(), data.policyNumber(), data.paymentDueDate(), data.premiumAmount());
        gateway.send(data.email(), "保單續期提醒", body);
        String message = "保單末四碼 %s 的續期 Email 已送出".formatted(
                PersonalDataMasker.policySuffix(data.policyNumber()));
        return StrategyResultFactory.success(data, PersonalDataMasker.email(data.email()), message);
    }
}
