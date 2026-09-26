package com.stockcash.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Formatea fechas y horas para mostrar en la UI en español. */
public final class DateFormatter {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE      = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME      = DateTimeFormatter.ofPattern("HH:mm:ss");

    private DateFormatter() {}

    public static String formatDateTime(LocalDateTime dt) {
        return dt == null ? "" : dt.format(DATETIME);
    }

    public static String formatDate(LocalDateTime dt) {
        return dt == null ? "" : dt.format(DATE);
    }

    public static String formatTime(LocalDateTime dt) {
        return dt == null ? "" : dt.format(TIME);
    }
}
