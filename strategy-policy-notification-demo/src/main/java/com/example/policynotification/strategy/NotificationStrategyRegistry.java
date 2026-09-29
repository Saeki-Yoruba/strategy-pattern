package com.example.policynotification.strategy;

import com.example.policynotification.exception.DuplicateNotificationStrategyException;
import com.example.policynotification.exception.UnsupportedNotificationTypeException;
import com.example.policynotification.model.NotificationType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationStrategyRegistry {

    private final Map<NotificationType, NotificationStrategy> strategies;

    public NotificationStrategyRegistry(List<NotificationStrategy> strategyList) {
        EnumMap<NotificationType, NotificationStrategy> registered = new EnumMap<>(NotificationType.class);
        for (NotificationStrategy strategy : strategyList) {
            NotificationStrategy duplicated = registered.putIfAbsent(strategy.supports(), strategy);
            if (duplicated != null) {
                // 啟動時提早失敗，避免執行期間才發現同一類型對應兩種不確定行為。
                throw new DuplicateNotificationStrategyException(
                        "通知策略重複註冊：" + strategy.supports());
            }
        }
        this.strategies = Collections.unmodifiableMap(registered);
    }

    public NotificationStrategy get(NotificationType type) {
        NotificationStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new UnsupportedNotificationTypeException("找不到通知策略：" + type);
        }
        return strategy;
    }
}
