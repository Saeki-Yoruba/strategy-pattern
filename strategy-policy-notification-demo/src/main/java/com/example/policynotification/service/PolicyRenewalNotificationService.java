package com.example.policynotification.service;

import com.example.policynotification.model.*;
import com.example.policynotification.provider.PolicyRenewalNotificationProvider;
import com.example.policynotification.strategy.NotificationStrategy;
import com.example.policynotification.strategy.NotificationStrategyRegistry;
import com.example.policynotification.support.PersonalDataMasker;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PolicyRenewalNotificationService {

    private final PolicyRenewalNotificationProvider provider;
    private final NotificationStrategyRegistry registry;

    public PolicyRenewalNotificationService(
            PolicyRenewalNotificationProvider provider,
            NotificationStrategyRegistry registry
    ) {
        this.provider = provider;
        this.registry = registry;
    }

    public PolicyRenewalBatchResult send(PolicyRenewalBatchRequest request) {
        validate(request);

        Set<PolicyCustomerKey> keys = request.policies().stream()
                .map(PolicyCustomerRequest::toKey)
                .collect(java.util.stream.Collectors.toSet());
        Map<PolicyCustomerKey, PolicyRenewalNotificationData> dataMap = provider.findAll(keys);

        // 依原始輸入順序處理，使批次結果能直接對回呼叫端的每一筆資料。
        List<NotificationResult> results = request.policies().stream()
                .map(item -> processOne(item, dataMap))
                .toList();

        return summarize(results);
    }

    private NotificationResult processOne(
            PolicyCustomerRequest item,
            Map<PolicyCustomerKey, PolicyRenewalNotificationData> dataMap
    ) {
        PolicyRenewalNotificationData data = dataMap.get(item.toKey());
        if (data == null) {
            return result(item, null, NotificationStatus.NOT_FOUND,
                    "查無符合的保單與客戶資料");
        }

        try {
            NotificationStrategy strategy = registry.get(data.notificationType());
            return strategy.send(data);
        } catch (RuntimeException exception) {
            // 本範例採部分成功：單筆通知失敗不應阻斷其他保單。
            return result(item, data.notificationType(), NotificationStatus.FAILED,
                    "通知失敗：" + exception.getMessage());
        }
    }

    private NotificationResult result(
            PolicyCustomerRequest item,
            NotificationType type,
            NotificationStatus status,
            String message
    ) {
        return new NotificationResult(
                item.policyNumber(), PersonalDataMasker.customerId(item.customerId()),
                type, status, "-", message, LocalDateTime.now());
    }

    private PolicyRenewalBatchResult summarize(List<NotificationResult> results) {
        int success = count(results, NotificationStatus.SUCCESS);
        int failed = count(results, NotificationStatus.FAILED);
        int notFound = count(results, NotificationStatus.NOT_FOUND);
        return new PolicyRenewalBatchResult(results.size(), success, failed, notFound, results);
    }

    private int count(List<NotificationResult> results, NotificationStatus status) {
        return (int) results.stream().filter(result -> result.status() == status).count();
    }

    private void validate(PolicyRenewalBatchRequest request) {
        if (request == null || request.policies() == null || request.policies().isEmpty()) {
            throw new IllegalArgumentException("保單清單不可為空");
        }
        Set<PolicyCustomerKey> uniqueKeys = new HashSet<>();
        for (PolicyCustomerRequest item : request.policies()) {
            if (item == null || isBlank(item.policyNumber()) || isBlank(item.customerId())) {
                throw new IllegalArgumentException("保單號碼與客戶證號不可為空");
            }
            if (!uniqueKeys.add(item.toKey())) {
                throw new IllegalArgumentException("保單與客戶資料不可重複：" + item.policyNumber());
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
