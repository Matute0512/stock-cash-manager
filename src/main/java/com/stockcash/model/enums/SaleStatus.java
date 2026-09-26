package com.stockcash.model.enums;

public enum SaleStatus {
    COMPLETED("Completada"), CANCELLED("Anulada");
    private final String label;
    SaleStatus(String l) { this.label = l; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
}
