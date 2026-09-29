package com.example.policynotification.exception;

public class UnsupportedNotificationTypeException extends RuntimeException {
    public UnsupportedNotificationTypeException(String message) {
        super(message);
    }
}
