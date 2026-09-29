package com.example.policynotification.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 應用服務的輸入，只表達「哪些保單需要處理」。
 * 通知方式屬於保戶偏好或系統設定，不開放呼叫端任意指定。
 */
public record PolicyRenewalBatchRequest(List<PolicyCustomerRequest> policies) {

    public PolicyRenewalBatchRequest {
        // 保留 null item 交由應用服務提供一致且可理解的輸入錯誤，集合本身仍採防禦性複製。
        policies = policies == null
                ? null
                : Collections.unmodifiableList(new ArrayList<>(policies));
    }
}
