package com.example.policynotification.exception;

public class UnsupportedNotificationTypeException extends NotificationProcessingException {
    public UnsupportedNotificationTypeException(String message) {
        super(message);
    }
}
