package util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Centralized input validation and parsing shared by the console menu, the GUI, and the web API. */
public final class Validation {
    private Validation() {
    }

    public static double parseAmount(String input) {
        double amount;
        try {
            amount = Double.parseDouble(input.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a valid positive amount.");
        }
        requirePositive(amount, "Amount");
        return amount;
    }

    public static LocalDate parseDateOrToday(String input) {
        String trimmed = input == null ? "" : input.trim();
        if (trimmed.isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(trimmed);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Date must be in YYYY-MM-DD format.");
        }
    }

    public static double parsePercent(String input, double min, double max) {
        double value;
        try {
            value = Double.parseDouble(input.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a valid number.");
        }
        requireRange(value, min, max, "Value");
        return value;
    }

    public static void requirePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
    }

    public static void requireRange(double value, double min, double max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    String.format("%s must be between %.0f and %.0f.", fieldName, min, max));
        }
    }

    public static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
    }
}
