package com.nimble.payment_gateway.util;

public class CreditCardMasker {

    private CreditCardMasker() {
        throw new IllegalStateException("Utility class");
    }

    public static String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }
}