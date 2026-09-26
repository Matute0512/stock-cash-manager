package com.stockcash.model.enums;

public enum Role {
    ADMIN("Administrador"), SUPERVISOR("Supervisor"), CAJERO("Cajero");
    private final String displayName;
    Role(String d) { this.displayName = d; }
    public String getDisplayName() { return displayName; }
    @Override public String toString() { return displayName; }
}
