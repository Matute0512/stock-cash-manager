package com.stockcash.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/** Formatea montos de dinero para mostrar en la UI. */
public final class CurrencyFormatter {

    private static final Locale LOCALE_AR = new Locale("es", "AR");
    private static final NumberFormat FORMAT = NumberFormat.getCurrencyInstance(LOCALE_AR);

    private CurrencyFormatter() {}

    /** Ej: BigDecimal("1234.50") → "\$ 1.234,50" */
    public static String format(BigDecimal amount) {
        if (amount == null) return "0,00";
        return FORMAT.format(amount);
    }

    /** Ej: BigDecimal("1234.50") → "1.234,50" (sin símbolo) */
    public static String formatPlain(BigDecimal amount) {
        if (amount == null) return "0,00";
        return String.format(LOCALE_AR, "%.2f", amount);
    }
}
