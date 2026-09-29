package com.example.policynotification.gateway;

/** 外部簡訊服務邊界；測試可替換此介面而不連接真實服務。 */
public interface SmsGateway {
    void send(String mobileNumber, String content);
}
