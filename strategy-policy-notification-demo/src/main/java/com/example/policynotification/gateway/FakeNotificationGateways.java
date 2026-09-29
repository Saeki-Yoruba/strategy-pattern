package com.example.policynotification.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway 隔離外部系統整合；本範例以無副作用的 Fake Bean 取代真實廠商服務。
 * Gateway 是外部邊界，不是用來選擇業務流程的 Strategy。
 */
@Configuration
public class FakeNotificationGateways {

    @Bean
    EmailGateway emailGateway() {
        return (email, subject, body) -> { };
    }

    @Bean
    SmsGateway smsGateway() {
        return (mobile, content) -> { };
    }

    @Bean
    PrintTaskGateway printTaskGateway() {
        return (address, content) -> { };
    }

    @Bean
    AppPushGateway appPushGateway() {
        return (token, title, content) -> { };
    }
}
