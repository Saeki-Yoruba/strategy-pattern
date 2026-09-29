package com.example.policynotification.model;

public record PolicyCustomerRequest(String policyNumber, String customerId) {

    public PolicyCustomerKey toKey() {
        return new PolicyCustomerKey(policyNumber, customerId);
    }
}
