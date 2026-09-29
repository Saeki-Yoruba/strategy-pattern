package com.example.policynotification.provider;

import com.example.policynotification.model.NotificationType;
import com.example.policynotification.model.PolicyCustomerKey;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FakePolicyRenewalNotificationProviderTest {

    private final FakePolicyRenewalNotificationProvider provider =
            new FakePolicyRenewalNotificationProvider();

    @Test
    void shouldReturnOnlyRequestedPolicies() {
        PolicyCustomerKey emailKey = new PolicyCustomerKey("PL20260001", "A123456789");
        PolicyCustomerKey smsKey = new PolicyCustomerKey("PL20260002", "B223456781");

        var result = provider.findAll(Set.of(emailKey, smsKey));

        assertThat(result).hasSize(2);
        assertThat(result.get(emailKey).notificationType()).isEqualTo(NotificationType.EMAIL);
        assertThat(result.get(smsKey).notificationType()).isEqualTo(NotificationType.SMS);
        assertThatThrownBy(result::clear).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldIgnoreUnknownKey() {
        var result = provider.findAll(Set.of(new PolicyCustomerKey("UNKNOWN", "Z999999999")));
        assertThat(result).isEmpty();
    }
}
