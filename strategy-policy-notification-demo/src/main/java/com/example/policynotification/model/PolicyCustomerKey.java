package com.example.policynotification.model;

/**
 * 保單號碼不足以識別通知對象，因此用保單號碼與客戶證號組成查詢 Key。
 * 正式系統可將這個 Key 交給資料庫或內部 API 查詢。
 */
public record PolicyCustomerKey(String policyNumber, String customerId) {
}
