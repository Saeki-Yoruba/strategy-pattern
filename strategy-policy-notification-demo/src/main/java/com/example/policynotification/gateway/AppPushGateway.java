package com.example.policynotification.gateway;

public interface AppPushGateway {
    void push(String deviceToken, String title, String content);
}
