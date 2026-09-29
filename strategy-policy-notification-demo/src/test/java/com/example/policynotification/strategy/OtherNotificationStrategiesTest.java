package com.example.policynotification.strategy;

import com.example.policynotification.gateway.AppPushGateway;
import com.example.policynotification.gateway.EmailGateway;
import com.example.policynotification.gateway.PrintTaskGateway;
import com.example.policynotification.model.NotificationStatus;
import com.example.policynotification.model.NotificationType;
import com.example.policynotification.model.PolicyRenewalNotificationData;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OtherNotificationStrategiesTest {

    @Test
    void shouldSendEmailWithMaskedDestination() {
        var strategy = new EmailNotificationStrategy(mock(EmailGateway.class));
        var result = strategy.send(data(NotificationType.EMAIL,
                "user@example.com", null, null, null));
        assertThat(result.status()).isEqualTo(NotificationStatus.SUCCESS);
        assertThat(result.maskedDestination()).doesNotContain("user@example.com");
    }

    @Test
    void shouldCreatePaperMailTaskWithMaskedAddress() {
        var strategy = new PaperMailNotificationStrategy(mock(PrintTaskGateway.class));
        var result = strategy.send(data(NotificationType.PAPER_MAIL,
                null, null, "台北市內湖區範例路100號", null));
        assertThat(result.maskedDestination()).isEqualTo("台北市內湖區範例路***號");
    }

    @Test
    void shouldPushAppNotificationWithMaskedToken() {
        var strategy = new AppPushNotificationStrategy(mock(AppPushGateway.class));
        var result = strategy.send(data(NotificationType.APP_PUSH,
                null, null, null, "demo-device-token-0004"));
        assertThat(result.maskedDestination()).isEqualTo("APP-DEVICE-***0004");
    }

    private PolicyRenewalNotificationData data(
            NotificationType type, String email, String mobile, String address, String token) {
        return new PolicyRenewalNotificationData(
                "PL20260001", "A123456789", "王小明",
                LocalDate.of(2026, 9, 30), new BigDecimal("12000"),
                type, email, mobile, address, token
        );
    }
}
