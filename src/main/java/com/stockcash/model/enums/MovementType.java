package com.stockcash.model.enums;

public enum MovementType {
    OPENING, SALE_CASH, SALE_CARD, SALE_TRANSFER,
    SALE_REFUND_CASH, MANUAL_INCOME, MANUAL_EXPENSE;

    public boolean affectsCashBalance() {
        return this == OPENING || this == SALE_CASH ||
               this == MANUAL_INCOME || this == SALE_REFUND_CASH ||
               this == MANUAL_EXPENSE;
    }

    public int getSignMultiplier() {
        return switch (this) {
            case OPENING, SALE_CASH, MANUAL_INCOME   ->  1;
            case SALE_REFUND_CASH, MANUAL_EXPENSE    -> -1;
            default                                   ->  0;
        };
    }
}
