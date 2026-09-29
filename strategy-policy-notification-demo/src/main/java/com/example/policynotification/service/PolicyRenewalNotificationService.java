package com.example.policynotification.service;

import com.example.policynotification.exception.NotificationProcessingException;
import com.example.policynotification.exception.UnsupportedNotificationTypeException;
import com.example.policynotification.model.NotificationResult;
import com.example.policynotification.model.NotificationStatus;
import com.example.policynotification.model.NotificationType;
import com.example.policynotification.model.PolicyCustomerKey;
import com.example.policynotification.model.PolicyCustomerRequest;
import com.example.policynotification.model.PolicyRenewalBatchRequest;
import com.example.policynotification.model.PolicyRenewalBatchResult;
import com.example.policynotification.model.PolicyRenewalNotificationData;
import com.example.policynotification.provider.PolicyRenewalNotificationProvider;
import com.example.policynotification.strategy.NotificationStrategy;
import com.example.policynotification.strategy.NotificationStrategyRegistry;
import com.example.policynotification.support.PersonalDataMasker;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
        validateRequest(request);

        Set<PolicyCustomerKey> keys = toKeys(request.policies());
        // Provider 採單次批次查詢，避免正式替換為資料庫後在逐筆流程中產生 N+1 查詢。
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
        } catch (NotificationProcessingException exception) {
            // 單筆通知屬於彼此獨立的工作，因此預期的單筆失敗不應阻斷後續保單。
            return result(item, data.notificationType(), NotificationStatus.FAILED,
                    "通知失敗：" + safeFailureReason(exception));
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

    private String safeFailureReason(NotificationProcessingException exception) {
        // 批次結果不回傳底層例外文字，避免未來外部整合例外意外夾帶完整聯絡資料。
        return exception instanceof UnsupportedNotificationTypeException
                ? "通知方式不支援"
                : "通知目的地不存在或格式錯誤";
    }

    private void validateRequest(PolicyRenewalBatchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("批次請求不可為 null");
        }
        validatePolicies(request.policies());
    }

    private void validatePolicies(List<PolicyCustomerRequest> policies) {
        if (policies == null) {
            throw new IllegalArgumentException("保單清單不可為 null");
        }
        if (policies.isEmpty()) {
            throw new IllegalArgumentException("保單清單不可為空");
        }
        Set<PolicyCustomerKey> uniqueKeys = new HashSet<>();
        for (PolicyCustomerRequest item : policies) {
            validatePolicy(item);
            if (!uniqueKeys.add(item.toKey())) {
                throw new IllegalArgumentException("保單與客戶資料不可重複：" + item.policyNumber());
            }
        }
    }

    private void validatePolicy(PolicyCustomerRequest item) {
        if (item == null) {
            throw new IllegalArgumentException("保單項目不可為 null");
        }
        if (isBlank(item.policyNumber())) {
            throw new IllegalArgumentException("保單號碼不可為空");
        }
        if (isBlank(item.customerId())) {
            throw new IllegalArgumentException("客戶證號不可為空");
        }
    }

    private Set<PolicyCustomerKey> toKeys(List<PolicyCustomerRequest> policies) {
        Set<PolicyCustomerKey> keys = new LinkedHashSet<>();
        for (PolicyCustomerRequest policy : policies) {
            keys.add(policy.toKey());
        }
        return Set.copyOf(keys);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
