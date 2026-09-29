package com.example.policynotification.service;

import com.example.policynotification.model.*;
import com.example.policynotification.provider.PolicyRenewalNotificationProvider;
import com.example.policynotification.strategy.NotificationStrategy;
import com.example.policynotification.strategy.NotificationStrategyRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

class PolicyRenewalNotificationServiceTest {

    @Test
    void shouldQueryProviderOnceAndDelegateToStrategy() {
        PolicyRenewalNotificationProvider provider = mock(PolicyRenewalNotificationProvider.class);
        NotificationStrategyRegistry registry = mock(NotificationStrategyRegistry.class);
        NotificationStrategy strategy = mock(NotificationStrategy.class);
        PolicyRenewalNotificationService service = new PolicyRenewalNotificationService(provider, registry);

        PolicyCustomerRequest item = new PolicyCustomerRequest("PL20260001", "A123456789");
        PolicyRenewalNotificationData data = data();
        NotificationResult sent = new NotificationResult(
                data.policyNumber(), "A12******9", NotificationType.EMAIL,
                NotificationStatus.SUCCESS, "u***@example.com", "已送出", LocalDateTime.now());

        when(provider.findAll(anySet())).thenReturn(Map.of(item.toKey(), data));
        when(registry.get(NotificationType.EMAIL)).thenReturn(strategy);
        when(strategy.send(data)).thenReturn(sent);

        PolicyRenewalBatchResult result = service.send(new PolicyRenewalBatchRequest(List.of(item)));

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.results()).containsExactly(sent);
        verify(provider, times(1)).findAll(anySet());
        verify(registry).get(NotificationType.EMAIL);
        verify(strategy).send(data);
    }

    @Test
    void shouldReturnNotFoundWithoutStoppingBatch() {
        PolicyRenewalNotificationProvider provider = mock(PolicyRenewalNotificationProvider.class);
        NotificationStrategyRegistry registry = mock(NotificationStrategyRegistry.class);
        PolicyRenewalNotificationService service = new PolicyRenewalNotificationService(provider, registry);
        when(provider.findAll(anySet())).thenReturn(Map.of());

        PolicyRenewalBatchResult result = service.send(new PolicyRenewalBatchRequest(List.of(
                new PolicyCustomerRequest("UNKNOWN-1", "A123456789"),
                new PolicyCustomerRequest("UNKNOWN-2", "B223456781")
        )));

        assertThat(result.notFoundCount()).isEqualTo(2);
        assertThat(result.results()).extracting(NotificationResult::status)
                .containsOnly(NotificationStatus.NOT_FOUND);
        verifyNoInteractions(registry);
    }

    private PolicyRenewalNotificationData data() {
        return new PolicyRenewalNotificationData(
                "PL20260001", "A123456789", "王小明",
                LocalDate.of(2026, 9, 30), new BigDecimal("12000"),
                NotificationType.EMAIL, "user@example.com", null, null, null
        );
    }
}
