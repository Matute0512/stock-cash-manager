package com.stockcash.model.enums;

public enum PaymentMethod {
    CASH("Efectivo"), CARD("Tarjeta"), TRANSFER("Transferencia");
    private final String label;
    PaymentMethod(String l) { this.label = l; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
}
