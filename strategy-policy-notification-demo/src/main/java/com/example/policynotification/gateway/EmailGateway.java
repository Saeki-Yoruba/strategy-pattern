package com.example.policynotification.gateway;

/** 外部 Email 服務邊界；它不是用來表達業務處理方式的 Strategy。 */
public interface EmailGateway {
    void send(String email, String subject, String body);
}
