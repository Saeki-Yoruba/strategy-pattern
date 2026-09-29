package com.example.policynotification.gateway;

/** 外部 App 推播服務邊界；測試與 Demo 使用無副作用的 Fake。 */
public interface AppPushGateway {
    void push(String deviceToken, String title, String content);
}
