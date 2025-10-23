package com.nimble.payment_gateway.enums;

public enum ChargeStatus {
    PENDING("Pendente"),
    PAID("Paga"),
    CANCELLED("Cancelada");

    private final String description;

    ChargeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}