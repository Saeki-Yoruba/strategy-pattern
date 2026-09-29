package com.example.policynotification.exception;

public class InvalidNotificationDestinationException extends RuntimeException {
    public InvalidNotificationDestinationException(String message) {
        super(message);
    }
}
