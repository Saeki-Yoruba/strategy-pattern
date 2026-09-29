package com.example.policynotification.model;

import java.util.List;

public record PolicyRenewalBatchResult(
        int totalCount,
        int successCount,
        int failedCount,
        int notFoundCount,
        List<NotificationResult> results
) {

    public PolicyRenewalBatchResult {
        results = List.copyOf(results);
    }
}
