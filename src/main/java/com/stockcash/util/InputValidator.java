package com.stockcash.util;

import com.stockcash.exception.ValidationException;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Validaciones de entrada centralizadas (NFR-SEC-03).
 * Los Services llaman a estos métodos ANTES de procesar datos.
 * Lanza ValidationException con mensajes descriptivos en español (NFR-USA-02).
 */
public final class InputValidator {

    private static final Pattern USERNAME_PATTERN =
        Pattern.compile("^[a-zA-Z0-9_]{3,50}");
    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,100}");
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
    private static final Pattern TAX_ID_PATTERN =
        Pattern.compile("^\\d{2}-\\d{8}-\\d{1}");       // CUIT: XX-XXXXXXXX-X
    private static final Pattern SKU_PATTERN =
        Pattern.compile("^[a-zA-Z0-9\\-_]{2,50}");
    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^[+\\d\\s\\(\\)\\-]{6,30}");

    private InputValidator() {}

    // ── Usuarios ─────────────────────────────────────────────────────────────

    public static void validateUsername(String username) {
        if (username == null || username.isBlank())
            throw new ValidationException("El nombre de usuario es obligatorio.");
        if (!USERNAME_PATTERN.matcher(username.trim()).matches())
            throw new ValidationException(
                "El usuario debe tener 3-50 caracteres. Solo letras, números y guión bajo.");
    }

    public static void validatePassword(String password) {
        if (password == null || password.isBlank())
            throw new ValidationException("La contraseña es obligatoria.");
        if (!PASSWORD_PATTERN.matcher(password).matches())
            throw new ValidationException(
                "La contraseña debe tener al menos 8 caracteres, " +
                "una mayúscula, un número y un carácter especial.");
    }

    public static void validateFullName(String name) {
        if (name == null || name.isBlank())
            throw new ValidationException("El nombre completo es obligatorio.");
        String trimmed = name.trim();
        if (trimmed.length() < 2 || trimmed.length() > 100)
            throw new ValidationException("El nombre debe tener entre 2 y 100 caracteres.");
    }

    // ── Productos ─────────────────────────────────────────────────────────────

    public static void validateSku(String sku) {
        if (sku == null || sku.isBlank())
            throw new ValidationException("El código SKU es obligatorio.");
        if (!SKU_PATTERN.matcher(sku.trim()).matches())
            throw new ValidationException(
                "El SKU debe tener 2-50 caracteres. Solo letras, números, guión y guión bajo.");
    }

    public static void validatePrice(BigDecimal price, String fieldName) {
        if (price == null)
            throw new ValidationException("El " + fieldName + " es obligatorio.");
        if (price.compareTo(BigDecimal.ZERO) < 0)
            throw new ValidationException("El " + fieldName + " no puede ser negativo.");
        if (price.precision() - price.scale() > 12)
            throw new ValidationException("El " + fieldName + " excede el máximo permitido.");
    }

    public static void validateStock(int stock) {
        if (stock < 0)
            throw new ValidationException("El stock no puede ser negativo (BR-02).");
    }

    public static void validateMinStock(int minStock) {
        if (minStock < 0)
            throw new ValidationException("El stock mínimo no puede ser negativo.");
    }

    // ── Proveedores ───────────────────────────────────────────────────────────

    public static void validateTaxId(String taxId) {
        if (taxId == null || taxId.isBlank())
            throw new ValidationException("El CUIT es obligatorio.");
        if (!TAX_ID_PATTERN.matcher(taxId.trim()).matches())
            throw new ValidationException("El CUIT debe tener formato XX-XXXXXXXX-X.");
    }

    public static void validateEmail(String email) {
        if (email == null || email.isBlank()) return; // email es opcional
        if (!EMAIL_PATTERN.matcher(email.trim()).matches())
            throw new ValidationException("El email no tiene un formato válido.");
    }

    public static void validatePhone(String phone) {
        if (phone == null || phone.isBlank()) return; // teléfono es opcional
        if (!PHONE_PATTERN.matcher(phone.trim()).matches())
            throw new ValidationException("El teléfono no tiene un formato válido.");
    }

    // ── Caja ──────────────────────────────────────────────────────────────────

    public static void validateAmount(BigDecimal amount, String fieldName) {
        if (amount == null)
            throw new ValidationException("El " + fieldName + " es obligatorio.");
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new ValidationException("El " + fieldName + " no puede ser negativo.");
    }

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank())
            throw new ValidationException("El campo '" + fieldName + "' es obligatorio.");
    }

    /** Sanitiza: trim + elimina caracteres de control (NFR-SEC-03). */
    public static String sanitize(String input) {
        if (input == null) return null;
        return input.trim().replaceAll("[\\p{Cntrl}]", "");
    }
}
