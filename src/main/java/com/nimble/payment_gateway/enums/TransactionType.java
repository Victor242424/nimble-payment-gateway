package com.nimble.payment_gateway.enums;

public enum TransactionType {
    DEPOSIT("Depósito"),
    PAYMENT("Pagamento"),
    CREDIT("Crédito"),
    DEBIT("Débito"),
    REFUND("Estorno");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}