package com.example.policynotification.strategy;

import com.example.policynotification.exception.InvalidNotificationDestinationException;
import com.example.policynotification.gateway.SmsGateway;
import com.example.policynotification.model.NotificationStatus;
import com.example.policynotification.model.NotificationType;
import com.example.policynotification.model.PolicyRenewalNotificationData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmsNotificationStrategyTest {

    @Mock
    private SmsGateway gateway;

    @Test
    void shouldMaskSensitiveDataAndSendSms() {
        SmsNotificationStrategy strategy = new SmsNotificationStrategy(gateway);

        var result = strategy.send(data("0912345678"));

        ArgumentCaptor<String> content = ArgumentCaptor.forClass(String.class);
        verify(gateway).send(org.mockito.ArgumentMatchers.eq("0912345678"), content.capture());
        assertThat(content.getValue()).contains("0002").doesNotContain("PL20260002");
        assertThat(result.status()).isEqualTo(NotificationStatus.SUCCESS);
        assertThat(result.maskedDestination()).isEqualTo("*******678");
        assertThat(result.maskedCustomerId()).doesNotContain("B223456781");
    }

    @Test
    void shouldRejectInvalidMobileNumber() {
        SmsNotificationStrategy strategy = new SmsNotificationStrategy(gateway);
        assertThatThrownBy(() -> strategy.send(data("123")))
                .isInstanceOf(InvalidNotificationDestinationException.class);
    }

    private PolicyRenewalNotificationData data(String mobile) {
        return new PolicyRenewalNotificationData(
                "PL20260002", "B223456781", "陳小華",
                LocalDate.of(2026, 10, 15), new BigDecimal("8600"),
                NotificationType.SMS, null, mobile, null, null
        );
    }
}
