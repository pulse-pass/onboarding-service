package com.pulsepass.onboardingservice.common;

public final class DocumentHandlers {

    private static final int[] CPF_WEIGHTS = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] CNPJ_WEIGHTS = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private DocumentHandlers() {
    }

    private static boolean isOnlyDigits(String value, int length) {
        if (value == null || value.length() != length) {
            return false;
        }
        return value.chars().allMatch(c -> c >= '0' && c <= '9');
    }

    private static boolean isRepeatedDigit(String value) {
        return value.chars().distinct().count() == 1;
    }

    private static int checkDigit(String value, int length, int[] weights) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += (value.charAt(i) - '0') * weights[weights.length - length + i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private static boolean hasValidCheckDigits(String value, int[] weights) {
        int base = weights.length;
        return checkDigit(value, base - 1, weights) == value.charAt(base - 1) - '0'
                && checkDigit(value, base, weights) == value.charAt(base) - '0';
    }

    public static boolean isValidCpf(String value) {
        if (!isOnlyDigits(value, 11) || isRepeatedDigit(value)) {
            return false;
        }
        return hasValidCheckDigits(value, CPF_WEIGHTS);
    }

    public static boolean isValidCnpj(String value) {
        if (!isOnlyDigits(value, 14) || isRepeatedDigit(value)) {
            return false;
        }
        return hasValidCheckDigits(value, CNPJ_WEIGHTS);
    }
}
