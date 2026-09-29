package com.example.policynotification.comparison;

import com.example.policynotification.gateway.*;
import com.example.policynotification.model.NotificationType;
import com.example.policynotification.model.PolicyRenewalNotificationData;

/**
 * 可運作的舊式寫法，用於簡報比較。if-else 並沒有錯；問題是通知管道增加後，
 * 這個類別會同時承擔選擇、檢核、格式化及外部呼叫等多種變動原因。
 */
public class LegacyNotificationService {

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

    public void send(PolicyRenewalNotificationData data) {
        if (data.notificationType() == NotificationType.EMAIL) {
            emailGateway.send(data.email(), "保單續期提醒", "保單即將到期");
        } else if (data.notificationType() == NotificationType.SMS) {
            smsGateway.send(data.mobileNumber(), "保單即將到期");
        } else if (data.notificationType() == NotificationType.PAPER_MAIL) {
            printTaskGateway.create(data.postalAddress(), "保單即將到期");
        } else if (data.notificationType() == NotificationType.APP_PUSH) {
            appPushGateway.push(data.appDeviceToken(), "保單續期提醒", "保單即將到期");
        } else {
            throw new IllegalArgumentException("不支援的通知方式：" + data.notificationType());
        }
    }
}
