package com.stockcash.model.enums;

public enum CashRegisterStatus {
    OPEN("Abierta"), CLOSED("Cerrada"), REQUIRES_REVIEW("Requiere revision");
    private final String label;
    CashRegisterStatus(String l) { this.label = l; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
}
