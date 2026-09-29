package com.example.policynotification.gateway;

public interface SmsGateway {
    void send(String mobileNumber, String content);
}
