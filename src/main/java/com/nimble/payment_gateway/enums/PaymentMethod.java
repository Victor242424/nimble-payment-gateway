package com.nimble.payment_gateway.enums;

public enum PaymentMethod {
    BALANCE("Saldo em Conta"),
    CREDIT_CARD("Cartão de Crédito");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}