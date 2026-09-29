package az.corbank.mscorbank.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

public record Operation(
        String id,
        LocalDate date,              // may be null when the bank gave no (parsable) date
        String counterparty,
        String description,
        BigDecimal amount,           // always positive; direction says which way the money moved
        String currency,
        OperationDirection direction,
        OperationStatus status
) {

    /** Case-insensitive match on counterparty or description; blank search matches everything. */
    public boolean matchesSearch(String search) {
        if (search == null || search.isBlank()) return true;
        String needle = search.toLowerCase(Locale.ROOT);
        return (counterparty != null && counterparty.toLowerCase(Locale.ROOT).contains(needle))
                || (description != null && description.toLowerCase(Locale.ROOT).contains(needle));
    }

    /** null direction means "no direction filter". */
    public boolean matchesDirection(OperationDirection wanted) {
        return wanted == null || wanted == direction;
    }
}
