package com.example.policynotification.comparison;

import com.example.policynotification.exception.InvalidNotificationDestinationException;
import com.example.policynotification.exception.UnsupportedNotificationTypeException;
import com.example.policynotification.gateway.AppPushGateway;
import com.example.policynotification.gateway.EmailGateway;
import com.example.policynotification.gateway.PrintTaskGateway;
import com.example.policynotification.gateway.SmsGateway;
import com.example.policynotification.model.NotificationResult;
import com.example.policynotification.model.NotificationStatus;
import com.example.policynotification.model.NotificationType;
import com.example.policynotification.model.PolicyRenewalNotificationData;
import com.example.policynotification.support.PersonalDataMasker;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 可運作的舊式寫法，用於簡報比較。if-else 並沒有錯；問題是通知管道增加後，
 * 這個類別會同時承擔選擇、檢核、格式化、外部呼叫與結果組裝等多種變動原因。
 */
public class LegacyNotificationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final EmailGateway emailGateway;
    private final SmsGateway smsGateway;
    private final PrintTaskGateway printTaskGateway;
    private final AppPushGateway appPushGateway;

    public LegacyNotificationService(EmailGateway emailGateway, SmsGateway smsGateway,
                                     PrintTaskGateway printTaskGateway, AppPushGateway appPushGateway) {
        this.emailGateway = emailGateway;
        this.smsGateway = smsGateway;
        this.printTaskGateway = printTaskGateway;
        this.appPushGateway = appPushGateway;
    }

    public NotificationResult send(PolicyRenewalNotificationData data) {
        String maskedDestination;
        String summary;
        if (data.notificationType() == NotificationType.EMAIL) {
            if (data.email() == null || !EMAIL_PATTERN.matcher(data.email()).matches()) {
                throw new InvalidNotificationDestinationException("Email 不存在或格式錯誤");
            }
            String body = "%s您好，保單%s將於%s到期，本期應繳保費為%,.0f元。".formatted(
                    data.customerName(), data.policyNumber(), data.paymentDueDate(), data.premiumAmount());
            emailGateway.send(data.email(), "保單續期提醒", body);
            maskedDestination = PersonalDataMasker.email(data.email());
            summary = "續期 Email 已送出";
        } else if (data.notificationType() == NotificationType.SMS) {
            if (data.mobileNumber() == null || !data.mobileNumber().matches("09\\d{8}")) {
                throw new InvalidNotificationDestinationException("手機號碼不存在或格式錯誤");
            }
            String suffix = PersonalDataMasker.policySuffix(data.policyNumber());
            smsGateway.send(data.mobileNumber(), "保單末四碼%s將於%s到期。".formatted(
                    suffix, data.paymentDueDate()));
            maskedDestination = PersonalDataMasker.mobile(data.mobileNumber());
            summary = "續期簡訊已送出";
        } else if (data.notificationType() == NotificationType.PAPER_MAIL) {
            if (data.postalAddress() == null || data.postalAddress().isBlank()) {
                throw new InvalidNotificationDestinationException("郵寄地址不存在");
            }
            printTaskGateway.create(data.postalAddress(), "保單%s續期通知".formatted(data.policyNumber()));
            maskedDestination = PersonalDataMasker.address(data.postalAddress());
            summary = "紙本通知列印任務已建立";
        } else if (data.notificationType() == NotificationType.APP_PUSH) {
            if (data.appDeviceToken() == null || data.appDeviceToken().isBlank()) {
                throw new InvalidNotificationDestinationException("App 裝置識別不存在");
            }
            String suffix = PersonalDataMasker.policySuffix(data.policyNumber());
            appPushGateway.push(data.appDeviceToken(), "保單續期提醒",
                    "保單末四碼%s即將到期。".formatted(suffix));
            maskedDestination = PersonalDataMasker.deviceToken(data.appDeviceToken());
            summary = "App 推播已送出";
        } else {
            throw new UnsupportedNotificationTypeException("不支援的通知方式：" + data.notificationType());
        }
        return new NotificationResult(
                data.policyNumber(), PersonalDataMasker.customerId(data.customerId()),
                data.notificationType(), NotificationStatus.SUCCESS, maskedDestination,
                summary, LocalDateTime.now());
    }
}
