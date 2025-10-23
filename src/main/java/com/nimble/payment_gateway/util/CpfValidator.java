package com.nimble.payment_gateway.util;

public class CpfValidator {

    private CpfValidator() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isValid(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) {
            return false;
        }

        // Verifica se todos os dígitos são iguais
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            // Calcula o primeiro dígito verificador
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int firstDigit = 11 - (sum % 11);
            if (firstDigit >= 10) {
                firstDigit = 0;
            }

            // Calcula o segundo dígito verificador
            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            int secondDigit = 11 - (sum % 11);
            if (secondDigit >= 10) {
                secondDigit = 0;
            }

            boolean first = Character.getNumericValue(cpf.charAt(9)) == firstDigit;
            boolean second = Character.getNumericValue(cpf.charAt(10)) == secondDigit;

            // Verifica se os dígitos calculados conferem
            return Character.getNumericValue(cpf.charAt(9)) == firstDigit
                    && Character.getNumericValue(cpf.charAt(10)) == secondDigit;
        } catch (Exception e) {
            return false;
        }
    }
}
