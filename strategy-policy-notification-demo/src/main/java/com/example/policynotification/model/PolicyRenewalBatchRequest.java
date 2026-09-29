package com.example.policynotification.model;

import java.util.List;

/**
 * 應用服務的輸入，只表達「哪些保單需要處理」。
 * 通知方式屬於保戶偏好或系統設定，不開放呼叫端任意指定。
 */
public record PolicyRenewalBatchRequest(List<PolicyCustomerRequest> policies) {

    public PolicyRenewalBatchRequest {
        policies = policies == null ? null : List.copyOf(policies);
    }
}
