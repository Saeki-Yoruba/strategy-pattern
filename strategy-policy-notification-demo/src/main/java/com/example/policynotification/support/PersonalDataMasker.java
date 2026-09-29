package com.example.policynotification.support;

public final class PersonalDataMasker {

    private PersonalDataMasker() {
    }

    public static String customerId(String value) {
        if (value == null || value.length() < 4) return "***";
        return value.substring(0, 3) + "******" + value.substring(value.length() - 1);
    }

    public static String email(String value) {
        int at = value.indexOf('@');
        if (at <= 0) return "***";
        String local = value.substring(0, at);
        String visible = local.substring(0, 1);
        return visible + "*".repeat(Math.max(3, local.length() - 1)) + value.substring(at);
    }

    public static String mobile(String value) {
        return "*".repeat(Math.max(0, value.length() - 3)) + value.substring(value.length() - 3);
    }

    public static String address(String value) {
        return value.replaceFirst("\\d+號.*$", "***號");
    }

    public static String deviceToken(String value) {
        String suffix = value.substring(Math.max(0, value.length() - 4));
        return "APP-DEVICE-***" + suffix;
    }

    public static String policySuffix(String policyNumber) {
        return policyNumber.substring(Math.max(0, policyNumber.length() - 4));
    }
}
