package com.example.policynotification.exception;

public class InvalidNotificationDestinationException extends NotificationProcessingException {
    public InvalidNotificationDestinationException(String message) {
        super(message);
    }
}
