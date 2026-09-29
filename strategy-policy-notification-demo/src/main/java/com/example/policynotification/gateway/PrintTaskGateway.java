package com.example.policynotification.gateway;

public interface PrintTaskGateway {
    void create(String postalAddress, String content);
}
