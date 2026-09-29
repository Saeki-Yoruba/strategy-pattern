package com.example.policynotification;

import com.example.policynotification.model.NotificationResult;
import com.example.policynotification.model.NotificationStatus;
import com.example.policynotification.model.NotificationType;
import com.example.policynotification.model.PolicyCustomerRequest;
import com.example.policynotification.model.PolicyRenewalBatchRequest;
import com.example.policynotification.model.PolicyRenewalBatchResult;
import com.example.policynotification.service.PolicyRenewalNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PolicyRenewalNotificationDemoTest {

    @Autowired
    private PolicyRenewalNotificationService service;

    @Test
    void shouldSendRenewalNotificationsUsingCustomerPreferences() {
        PolicyRenewalBatchResult result = service.send(demoRequest());

        // Console 輸出只服務現場展示；Assertion 才是測試是否通過的依據。
        printResult(result);

        assertThat(result.totalCount()).isEqualTo(4);
        assertThat(result.successCount()).isEqualTo(4);
        assertThat(result.failedCount()).isZero();
        assertThat(result.notFoundCount()).isZero();
        assertThat(result.results())
                .extracting(NotificationResult::notificationType)
                .containsExactly(NotificationType.EMAIL, NotificationType.SMS,
                        NotificationType.PAPER_MAIL, NotificationType.APP_PUSH);
        assertThat(result.results())
                .extracting(NotificationResult::status)
                .containsOnly(NotificationStatus.SUCCESS);
        assertThat(result.results())
                .extracting(NotificationResult::policyNumber)
                .containsExactly("PL20260001", "PL20260002", "PL20260003", "PL20260004");
        assertThat(result.results())
                .extracting(NotificationResult::maskedCustomerId)
                .noneMatch(id -> id.matches("[A-Z]\\d{9}"));
        assertThat(result.results().toString())
                .doesNotContain(
                        "A123456789", "B223456781", "C123456789", "D223456781",
                        "xiaoming.wang@example.com", "0912345678",
                        "台北市內湖區範例路100號", "demo-device-token-0004");
    }

    @Test
    void shouldContinueWhenOnePolicyIsNotFound() {
        PolicyRenewalBatchRequest request = new PolicyRenewalBatchRequest(List.of(
                new PolicyCustomerRequest("PL20260001", "A123456789"),
                new PolicyCustomerRequest("PL20260002", "B223456781"),
                new PolicyCustomerRequest("PL20260003", "C123456789"),
                new PolicyCustomerRequest("UNKNOWN", "Z999999999")
        ));

        PolicyRenewalBatchResult result = service.send(request);

        assertThat(result.totalCount()).isEqualTo(4);
        assertThat(result.successCount()).isEqualTo(3);
        assertThat(result.notFoundCount()).isEqualTo(1);
        assertThat(result.results().get(3).status()).isEqualTo(NotificationStatus.NOT_FOUND);
    }

    private PolicyRenewalBatchRequest demoRequest() {
        return new PolicyRenewalBatchRequest(List.of(
                new PolicyCustomerRequest("PL20260001", "A123456789"),
                new PolicyCustomerRequest("PL20260002", "B223456781"),
                new PolicyCustomerRequest("PL20260003", "C123456789"),
                new PolicyCustomerRequest("PL20260004", "D223456781")
        ));
    }

    private void printResult(PolicyRenewalBatchResult result) {
        System.out.printf("%s%n", "=".repeat(60));
        System.out.printf("保單續期通知 Strategy Pattern Demo%n");
        System.out.printf("%s%n", "=".repeat(60));
        System.out.printf("輸入筆數：%d%n%n", result.totalCount());

        for (int index = 0; index < result.results().size(); index++) {
            NotificationResult item = result.results().get(index);
            System.out.printf("[%d]%n", index + 1);
            System.out.printf("保單號碼：%s%n", item.policyNumber());
            System.out.printf("客戶證號：%s%n", item.maskedCustomerId());
            System.out.printf("通知方式：%s%n", item.notificationType());
            System.out.printf("通知目的地：%s%n", item.maskedDestination());
            System.out.printf("處理結果：%s%n", item.status());
            System.out.printf("處理說明：%s%n%n", item.messageSummary());
        }

        System.out.printf("%s%n", "-".repeat(60));
        System.out.printf("總筆數：%d%n", result.totalCount());
        System.out.printf("成功：%d%n", result.successCount());
        System.out.printf("失敗：%d%n", result.failedCount());
        System.out.printf("查無資料：%d%n", result.notFoundCount());
        System.out.printf("%s%n", "=".repeat(60));
    }
}
