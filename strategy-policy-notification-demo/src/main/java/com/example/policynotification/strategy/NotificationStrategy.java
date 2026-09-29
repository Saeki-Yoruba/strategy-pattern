package com.example.policynotification.strategy;

import com.example.policynotification.model.NotificationResult;
import com.example.policynotification.model.NotificationType;
import com.example.policynotification.model.PolicyRenewalNotificationData;

/**
 * 各實作都完成「發送續期通知」，但檢核、格式及外部管道不同。
 * supports 用來註冊能力；Strategy 本身不決定何時被選中。
 */
public interface NotificationStrategy {
    NotificationType supports();
    NotificationResult send(PolicyRenewalNotificationData data);
}
