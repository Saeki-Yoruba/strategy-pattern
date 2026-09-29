package com.example.policynotification.provider;

import com.example.policynotification.model.NotificationType;
import com.example.policynotification.model.PolicyCustomerKey;
import com.example.policynotification.model.PolicyRenewalNotificationData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FakePolicyRenewalNotificationProvider implements PolicyRenewalNotificationProvider {

    private final Map<PolicyCustomerKey, PolicyRenewalNotificationData> dataSource;

    public FakePolicyRenewalNotificationProvider() {
        Map<PolicyCustomerKey, PolicyRenewalNotificationData> source = new LinkedHashMap<>();
        put(source, data("PL20260001", "A123456789", "王小明",
                "2026-09-30", "12000", NotificationType.EMAIL,
                "xiaoming.wang@example.com", null, null, null));
        put(source, data("PL20260002", "B223456781", "陳小華",
                "2026-10-15", "8600", NotificationType.SMS,
                null, "0912345678", null, null));
        put(source, data("PL20260003", "C123456789", "林怡君",
                "2026-11-05", "23500", NotificationType.PAPER_MAIL,
                null, null, "台北市內湖區範例路100號", null));
        put(source, data("PL20260004", "D223456781", "張志豪",
                "2026-12-20", "15800", NotificationType.APP_PUSH,
                null, null, null, "demo-device-token-0004"));
        this.dataSource = Collections.unmodifiableMap(source);
    }

    @Override
    public Map<PolicyCustomerKey, PolicyRenewalNotificationData> findAll(Set<PolicyCustomerKey> keys) {
        if (keys == null || keys.isEmpty()) {
            return Map.of();
        }

        // 找不到的 Key 不放入結果，交由流程層決定單筆 NOT_FOUND 是否影響整批。
        return keys.stream()
                .filter(dataSource::containsKey)
                .collect(Collectors.toUnmodifiableMap(key -> key, dataSource::get));
    }

    private void put(Map<PolicyCustomerKey, PolicyRenewalNotificationData> source,
                     PolicyRenewalNotificationData data) {
        source.put(new PolicyCustomerKey(data.policyNumber(), data.customerId()), data);
    }

    private PolicyRenewalNotificationData data(
            String policyNumber,
            String customerId,
            String customerName,
            String dueDate,
            String premium,
            NotificationType type,
            String email,
            String mobile,
            String address,
            String deviceToken
    ) {
        return new PolicyRenewalNotificationData(
                policyNumber, customerId, customerName, LocalDate.parse(dueDate),
                new BigDecimal(premium), type, email, mobile, address, deviceToken
        );
    }
}
