package com.example.policynotification.strategy;

import com.example.policynotification.exception.DuplicateNotificationStrategyException;
import com.example.policynotification.exception.UnsupportedNotificationTypeException;
import com.example.policynotification.model.NotificationResult;
import com.example.policynotification.model.NotificationType;
import com.example.policynotification.model.PolicyRenewalNotificationData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationStrategyRegistryTest {

    @Test
    void shouldReturnRegisteredStrategy() {
        NotificationStrategy email = stub(NotificationType.EMAIL);
        NotificationStrategyRegistry registry = new NotificationStrategyRegistry(List.of(email));

        assertThat(registry.get(NotificationType.EMAIL)).isSameAs(email);
    }

    @Test
    void shouldRejectDuplicatedType() {
        assertThatThrownBy(() -> new NotificationStrategyRegistry(List.of(
                stub(NotificationType.EMAIL), stub(NotificationType.EMAIL))))
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

    private NotificationStrategy stub(NotificationType type) {
        return new NotificationStrategy() {
            @Override
            public NotificationType supports() {
                return type;
            }

            @Override
            public NotificationResult send(PolicyRenewalNotificationData data) {
                return null;
            }
        };
    }
}
