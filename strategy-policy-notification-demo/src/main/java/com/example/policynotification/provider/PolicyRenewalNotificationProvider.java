package com.example.policynotification.provider;

import com.example.policynotification.model.PolicyCustomerKey;
import com.example.policynotification.model.PolicyRenewalNotificationData;

import java.util.Map;
import java.util.Set;

/**
 * Provider 隔離資料來源。批次介面可避免正式替換為 DB 查詢後產生 N+1 問題。
 * Provider 只負責準備資料，不決定如何發送通知。
 */
public interface PolicyRenewalNotificationProvider {

    Map<PolicyCustomerKey, PolicyRenewalNotificationData> findAll(Set<PolicyCustomerKey> keys);
}
