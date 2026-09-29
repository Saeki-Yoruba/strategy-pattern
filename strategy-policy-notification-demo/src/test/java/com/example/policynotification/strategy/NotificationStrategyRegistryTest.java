package com.example.policynotification.strategy;

import com.example.policynotification.exception.DuplicateNotificationStrategyException;
import com.example.policynotification.exception.UnsupportedNotificationTypeException;
import com.example.policynotification.model.NotificationType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationStrategyRegistryTest {

    @Test
    void shouldReturnAllFourRegisteredStrategiesWithoutExecutingThem() {
        NotificationStrategy email = strategy(NotificationType.EMAIL);
        NotificationStrategy sms = strategy(NotificationType.SMS);
        NotificationStrategy paperMail = strategy(NotificationType.PAPER_MAIL);
        NotificationStrategy appPush = strategy(NotificationType.APP_PUSH);
        NotificationStrategyRegistry registry =
                new NotificationStrategyRegistry(List.of(email, sms, paperMail, appPush));

        assertThat(registry.get(NotificationType.EMAIL)).isSameAs(email);
        assertThat(registry.get(NotificationType.SMS)).isSameAs(sms);
        assertThat(registry.get(NotificationType.PAPER_MAIL)).isSameAs(paperMail);
        assertThat(registry.get(NotificationType.APP_PUSH)).isSameAs(appPush);
        verify(email, never()).send(org.mockito.ArgumentMatchers.any());
        verify(sms, never()).send(org.mockito.ArgumentMatchers.any());
        verify(paperMail, never()).send(org.mockito.ArgumentMatchers.any());
        verify(appPush, never()).send(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectDuplicatedTypeDuringConstruction() {
        assertThatThrownBy(() -> new NotificationStrategyRegistry(List.of(
                strategy(NotificationType.EMAIL), strategy(NotificationType.EMAIL))))
                .isInstanceOf(DuplicateNotificationStrategyException.class)
                .hasMessageContaining("EMAIL");
    }

    @Test
    void shouldRejectUnsupportedType() {
        NotificationStrategyRegistry registry = new NotificationStrategyRegistry(List.of());

        assertThatThrownBy(() -> registry.get(NotificationType.SMS))
                .isInstanceOf(UnsupportedNotificationTypeException.class)
                .hasMessageContaining("SMS");
    }

    @Test
    void shouldRejectNullTypeWithClearMessage() {
        NotificationStrategyRegistry registry = new NotificationStrategyRegistry(List.of());

        assertThatThrownBy(() -> registry.get(null))
                .isInstanceOf(UnsupportedNotificationTypeException.class)
                .hasMessageContaining("null");
    }

    private NotificationStrategy strategy(NotificationType type) {
        NotificationStrategy strategy = mock(NotificationStrategy.class);
        when(strategy.supports()).thenReturn(type);
        return strategy;
    }
}
