package com.pulsepass.onboardingservice.common;

import java.util.Random;

public final class DocumentGenerators {

    private static final Random RANDOM = new Random();
    private static final int[] CPF_WEIGHTS = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] CNPJ_WEIGHTS = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private DocumentGenerators() {
    }

    private static int calculateDigit(int[] digits, int length, int[] weights) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += digits[i] * weights[weights.length - length + i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private static String join(int[] digits) {
        StringBuilder sb = new StringBuilder();
        for (int digit : digits) {
            sb.append(digit);
        }
        return sb.toString();
    }

    public static String generateCpf() {
        int[] digits = new int[11];
        for (int i = 0; i < 9; i++) {
            digits[i] = RANDOM.nextInt(10);
        }
        digits[0] = 1 + RANDOM.nextInt(9);
        digits[1] = (digits[0] + 1) % 10;
        digits[9] = calculateDigit(digits, 9, CPF_WEIGHTS);
        digits[10] = calculateDigit(digits, 10, CPF_WEIGHTS);
        return join(digits);
    }

    public static String generateCnpj() {
        int[] digits = new int[14];
        for (int i = 0; i < 8; i++) {
            digits[i] = RANDOM.nextInt(10);
        }
        digits[0] = 1 + RANDOM.nextInt(9);
        digits[1] = (digits[0] + 1) % 10;
        digits[11] = 1;
        digits[12] = calculateDigit(digits, 12, CNPJ_WEIGHTS);
        digits[13] = calculateDigit(digits, 13, CNPJ_WEIGHTS);
        return join(digits);
    }
}
