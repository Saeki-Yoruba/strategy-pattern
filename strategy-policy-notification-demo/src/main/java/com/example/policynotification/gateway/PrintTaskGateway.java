package com.example.policynotification.gateway;

/** 外部列印／郵務系統邊界；本介面只建立列印任務。 */
public interface PrintTaskGateway {
    void create(String postalAddress, String content);
}
