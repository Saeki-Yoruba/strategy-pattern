package com.example.policynotification.gateway;

public interface EmailGateway {
    void send(String email, String subject, String body);
}
