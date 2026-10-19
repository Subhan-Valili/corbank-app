package az.corbank.mscorbank.domain.model;

import java.util.Optional;

/** Banks this system aggregates. Declaration order = display order. */
public enum Bank {
    PASHA("PASHA Bank"),
    ABB("ABB");

    private final String displayName;

    Bank(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    /** Case-insensitive lookup; blank or unknown codes yield empty. */
    public static Optional<Bank> fromCode(String code) {
        if (code == null) return Optional.empty();
        String normalized = code.trim();
        for (Bank bank : values()) {
            if (bank.name().equalsIgnoreCase(normalized)) return Optional.of(bank);
        }
        return Optional.empty();
    }
}
