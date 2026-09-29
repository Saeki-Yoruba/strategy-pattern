package com.example.policynotification.provider;

import com.example.policynotification.model.NotificationType;
import com.example.policynotification.model.PolicyCustomerKey;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FakePolicyRenewalNotificationProviderTest {

    private static final PolicyCustomerKey EMAIL_KEY = key("PL20260001", "A123456789");
    private static final PolicyCustomerKey SMS_KEY = key("PL20260002", "B223456781");
    private static final PolicyCustomerKey PAPER_MAIL_KEY = key("PL20260003", "C123456789");
    private static final PolicyCustomerKey APP_PUSH_KEY = key("PL20260004", "D223456781");

    private final FakePolicyRenewalNotificationProvider provider =
            new FakePolicyRenewalNotificationProvider();

    @Test
    void shouldReturnFourRequestedPoliciesWithExpectedNotificationTypes() {
        var result = provider.findAll(Set.of(EMAIL_KEY, SMS_KEY, PAPER_MAIL_KEY, APP_PUSH_KEY));

        assertThat(result).hasSize(4);
        assertThat(result.get(EMAIL_KEY).notificationType()).isEqualTo(NotificationType.EMAIL);
        assertThat(result.get(SMS_KEY).notificationType()).isEqualTo(NotificationType.SMS);
        assertThat(result.get(PAPER_MAIL_KEY).notificationType()).isEqualTo(NotificationType.PAPER_MAIL);
        assertThat(result.get(APP_PUSH_KEY).notificationType()).isEqualTo(NotificationType.APP_PUSH);
    }

    @Test
    void shouldReturnOnlyRequestedKeyAndIgnoreUnknownKey() {
        PolicyCustomerKey unknown = key("UNKNOWN", "Z999999999");

        var result = provider.findAll(Set.of(SMS_KEY, unknown));

        assertThat(result).containsOnlyKeys(SMS_KEY).doesNotContainKey(unknown);
    }

    @Test
    void shouldReturnEmptyMapForEmptyKeys() {
        assertThat(provider.findAll(Set.of())).isEmpty();
    }

    @Test
    void shouldReturnUnmodifiableMap() {
        var result = provider.findAll(Set.of(EMAIL_KEY));

        assertThatThrownBy(result::clear)
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static PolicyCustomerKey key(String policyNumber, String customerId) {
        return new PolicyCustomerKey(policyNumber, customerId);
    }
}
