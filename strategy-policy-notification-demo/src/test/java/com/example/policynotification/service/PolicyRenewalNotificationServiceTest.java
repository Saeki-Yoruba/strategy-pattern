package com.example.policynotification.service;

import com.example.policynotification.exception.InvalidNotificationDestinationException;
import com.example.policynotification.model.NotificationResult;
import com.example.policynotification.model.NotificationStatus;
import com.example.policynotification.model.NotificationType;
import com.example.policynotification.model.PolicyCustomerRequest;
import com.example.policynotification.model.PolicyRenewalBatchRequest;
import com.example.policynotification.model.PolicyRenewalNotificationData;
import com.example.policynotification.provider.PolicyRenewalNotificationProvider;
import com.example.policynotification.strategy.NotificationStrategy;
import com.example.policynotification.strategy.NotificationStrategyRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyRenewalNotificationServiceTest {

    @Mock
    private PolicyRenewalNotificationProvider provider;
    @Mock
    private NotificationStrategyRegistry registry;
    @Mock
    private NotificationStrategy emailStrategy;
    @Mock
    private NotificationStrategy smsStrategy;

    private PolicyRenewalNotificationService service;

    @BeforeEach
    void setUp() {
        service = new PolicyRenewalNotificationService(provider, registry);
    }

    @Test
    void shouldQueryProviderOnceInBatchAndPassProviderDataToSelectedStrategies() {
        PolicyCustomerRequest emailRequest = request("PL20260001", "A123456789");
        PolicyCustomerRequest smsRequest = request("PL20260002", "B223456781");
        PolicyRenewalNotificationData emailData = data(emailRequest, NotificationType.EMAIL);
        PolicyRenewalNotificationData smsData = data(smsRequest, NotificationType.SMS);
        when(provider.findAll(anySet())).thenReturn(Map.of(
                emailRequest.toKey(), emailData, smsRequest.toKey(), smsData));
        when(registry.get(NotificationType.EMAIL)).thenReturn(emailStrategy);
        when(registry.get(NotificationType.SMS)).thenReturn(smsStrategy);
        when(emailStrategy.send(emailData)).thenReturn(success(emailData));
        when(smsStrategy.send(smsData)).thenReturn(success(smsData));

        var result = service.send(new PolicyRenewalBatchRequest(List.of(emailRequest, smsRequest)));

        verify(provider, times(1)).findAll(Set.of(emailRequest.toKey(), smsRequest.toKey()));
        verify(registry).get(NotificationType.EMAIL);
        verify(registry).get(NotificationType.SMS);
        verify(emailStrategy).send(emailData);
        verify(smsStrategy).send(smsData);
        assertThat(result.results()).extracting(NotificationResult::policyNumber)
                .containsExactly("PL20260001", "PL20260002");
    }

    @Test
    void shouldPreserveRequestOrderWhenProviderMapHasDifferentOrder() {
        PolicyCustomerRequest first = request("PL20260001", "A123456789");
        PolicyCustomerRequest second = request("PL20260002", "B223456781");
        PolicyRenewalNotificationData firstData = data(first, NotificationType.EMAIL);
        PolicyRenewalNotificationData secondData = data(second, NotificationType.SMS);
        when(provider.findAll(anySet())).thenReturn(Map.of(second.toKey(), secondData, first.toKey(), firstData));
        when(registry.get(NotificationType.EMAIL)).thenReturn(emailStrategy);
        when(registry.get(NotificationType.SMS)).thenReturn(smsStrategy);
        when(emailStrategy.send(firstData)).thenReturn(success(firstData));
        when(smsStrategy.send(secondData)).thenReturn(success(secondData));

        var result = service.send(new PolicyRenewalBatchRequest(List.of(first, second)));

        assertThat(result.results()).extracting(NotificationResult::policyNumber)
                .containsExactly("PL20260001", "PL20260002");
    }

    @Test
    void shouldContinueAfterNotFoundAndCountEveryStatus() {
        PolicyCustomerRequest emailRequest = request("PL20260001", "A123456789");
        PolicyCustomerRequest missingRequest = request("UNKNOWN", "Z999999999");
        PolicyCustomerRequest smsRequest = request("PL20260002", "B223456781");
        PolicyRenewalNotificationData emailData = data(emailRequest, NotificationType.EMAIL);
        PolicyRenewalNotificationData smsData = data(smsRequest, NotificationType.SMS);
        when(provider.findAll(anySet())).thenReturn(Map.of(
                emailRequest.toKey(), emailData, smsRequest.toKey(), smsData));
        when(registry.get(NotificationType.EMAIL)).thenReturn(emailStrategy);
        when(registry.get(NotificationType.SMS)).thenReturn(smsStrategy);
        when(emailStrategy.send(emailData)).thenReturn(success(emailData));
        when(smsStrategy.send(smsData)).thenThrow(new InvalidNotificationDestinationException("手機號碼格式錯誤"));

        var result = service.send(new PolicyRenewalBatchRequest(
                List.of(smsRequest, missingRequest, emailRequest)));

        assertThat(result.totalCount()).isEqualTo(3);
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failedCount()).isEqualTo(1);
        assertThat(result.notFoundCount()).isEqualTo(1);
        assertThat(result.results()).extracting(NotificationResult::status)
                .containsExactly(NotificationStatus.FAILED, NotificationStatus.NOT_FOUND,
                        NotificationStatus.SUCCESS);
        assertThat(result.results().get(1).notificationType()).isNull();
        assertThat(result.results().get(0).messageSummary()).doesNotContain("0912345678");
    }

    @Test
    void shouldNotHideUnexpectedProgrammingErrors() {
        PolicyCustomerRequest item = request("PL20260001", "A123456789");
        PolicyRenewalNotificationData emailData = data(item, NotificationType.EMAIL);
        when(provider.findAll(anySet())).thenReturn(Map.of(item.toKey(), emailData));
        when(registry.get(NotificationType.EMAIL)).thenReturn(emailStrategy);
        when(emailStrategy.send(emailData)).thenThrow(new NullPointerException("unexpected bug"));

        assertThatThrownBy(() -> service.send(new PolicyRenewalBatchRequest(List.of(item))))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("unexpected bug");
    }

    @Test
    void shouldRejectNullAndEmptyRequestsBeforeCallingProvider() {
        assertThatThrownBy(() -> service.send(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("批次請求");
        assertThatThrownBy(() -> service.send(new PolicyRenewalBatchRequest(null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不可為 null");
        assertThatThrownBy(() -> service.send(new PolicyRenewalBatchRequest(List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不可為空");
        verifyNoInteractions(provider, registry);
    }

    @Test
    void shouldRejectNullItemAndBlankFields() {
        assertThatThrownBy(() -> service.send(new PolicyRenewalBatchRequest(
                java.util.Arrays.asList((PolicyCustomerRequest) null))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("保單項目");
        assertThatThrownBy(() -> service.send(new PolicyRenewalBatchRequest(
                List.of(request(" ", "A123456789")))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("保單號碼");
        assertThatThrownBy(() -> service.send(new PolicyRenewalBatchRequest(
                List.of(request("PL20260001", " ")))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("客戶證號");
        verifyNoInteractions(provider, registry);
    }

    @Test
    void shouldRejectDuplicatedPolicyCustomerKey() {
        PolicyCustomerRequest duplicate = request("PL20260001", "A123456789");

        assertThatThrownBy(() -> service.send(
                new PolicyRenewalBatchRequest(List.of(duplicate, duplicate))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不可重複")
                .hasMessageNotContaining("A123456789");
        verify(provider, never()).findAll(anySet());
        verifyNoInteractions(registry);
    }

    private PolicyCustomerRequest request(String policyNumber, String customerId) {
        return new PolicyCustomerRequest(policyNumber, customerId);
    }

    private PolicyRenewalNotificationData data(PolicyCustomerRequest item, NotificationType type) {
        return new PolicyRenewalNotificationData(
                item.policyNumber(), item.customerId(), "測試客戶",
                LocalDate.of(2026, 9, 30), new BigDecimal("12000"), type,
                type == NotificationType.EMAIL ? "user@example.com" : null,
                type == NotificationType.SMS ? "0912345678" : null,
                null, null);
    }

    private NotificationResult success(PolicyRenewalNotificationData data) {
        return new NotificationResult(
                data.policyNumber(), "A12******9", data.notificationType(),
                NotificationStatus.SUCCESS, "***", "已送出", LocalDateTime.now());
    }
}
