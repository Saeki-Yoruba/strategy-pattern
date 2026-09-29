package com.example.policynotification.strategy;

import com.example.policynotification.model.NotificationResult;
import com.example.policynotification.model.NotificationType;
import com.example.policynotification.model.PolicyRenewalNotificationData;

/**
 * {@link #supports()} 表示 Strategy 支援的通知類型；{@link #send(PolicyRenewalNotificationData)}
 * 是所有策略共同提供的能力。每個 Strategy 只負責單一通知方式的資料檢核、內容組裝與
 * Gateway 呼叫，不自行查詢保單資料、不決定自己何時被選中，也不控制整批是否中止。
 */
public interface NotificationStrategy {

    NotificationType supports();

    NotificationResult send(PolicyRenewalNotificationData data);
}
