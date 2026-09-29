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
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SmsNotificationStrategyTest {

    @Mock
    private SmsGateway gateway;

    @Test
    void shouldSendConciseSmsWithoutFullPolicyNumber() {
        SmsNotificationStrategy strategy = new SmsNotificationStrategy(gateway);

        var result = strategy.send(data("0912345678"));

        ArgumentCaptor<String> content = ArgumentCaptor.forClass(String.class);
        verify(gateway).send(org.mockito.ArgumentMatchers.eq("0912345678"), content.capture());
        assertThat(content.getValue())
                .contains("0002", "2026-10-15", "8,600元")
                .doesNotContain("PL20260002");
        assertThat(result.status()).isEqualTo(NotificationStatus.SUCCESS);
        assertThat(result.maskedDestination()).isEqualTo("*******678");
    }

    @Test
    void shouldRejectNullMobileNumber() {
        assertInvalidMobile(null);
    }

    @Test
    void shouldRejectBlankMobileNumber() {
        assertInvalidMobile("  ");
    }

    @Test
    void shouldRejectMalformedMobileNumber() {
        assertInvalidMobile("0812345678");
    }

    private void assertInvalidMobile(String mobile) {
        SmsNotificationStrategy strategy = new SmsNotificationStrategy(gateway);
        assertThatThrownBy(() -> strategy.send(data(mobile)))
                .isInstanceOf(InvalidNotificationDestinationException.class)
                .hasMessageNotContaining(String.valueOf(mobile));
        verifyNoInteractions(gateway);
    }

    private PolicyRenewalNotificationData data(String mobile) {
        return new PolicyRenewalNotificationData(
                "PL20260002", "B223456781", "陳小華",
                LocalDate.of(2026, 10, 15), new BigDecimal("8600"),
                NotificationType.SMS, null, mobile, null, null);
    }
}
