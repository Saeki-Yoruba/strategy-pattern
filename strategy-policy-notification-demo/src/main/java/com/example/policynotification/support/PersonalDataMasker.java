package com.example.policynotification.support;

public final class PersonalDataMasker {

    private static final String MASKED = "***";

    private PersonalDataMasker() {
    }

    public static String customerId(String value) {
        if (isBlank(value) || value.length() < 4) {
            return MASKED;
        }
        return value.substring(0, 3) + "******" + value.substring(value.length() - 1);
    }

    public static String email(String value) {
        if (isBlank(value)) {
            return MASKED;
        }
        int at = value.indexOf('@');
        if (at <= 0 || at == value.length() - 1) {
            return MASKED;
        }
        String local = value.substring(0, at);
        String visible = local.substring(0, 1);
        return visible + "*".repeat(Math.max(3, local.length() - 1)) + value.substring(at);
    }

    public static String mobile(String value) {
        if (isBlank(value) || value.length() <= 3) {
            return MASKED;
        }
        return "*".repeat(Math.max(0, value.length() - 3)) + value.substring(value.length() - 3);
    }

    public static String address(String value) {
        if (isBlank(value)) {
            return MASKED;
        }
        String maskedNumber = value.replaceFirst("\\d+號.*$", "***號");
        if (!maskedNumber.equals(value)) {
            return maskedNumber;
        }
        int visibleLength = Math.min(6, value.length());
        return value.substring(0, visibleLength) + MASKED;
    }

    public static String deviceToken(String value) {
        if (isBlank(value) || value.length() <= 4) {
            return "APP-DEVICE-***";
        }
        String suffix = value.substring(value.length() - 4);
        return "APP-DEVICE-***" + suffix;
    }

    public static String policySuffix(String policyNumber) {
        if (isBlank(policyNumber)) {
            return "****";
        }
        return policyNumber.substring(Math.max(0, policyNumber.length() - 4));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
