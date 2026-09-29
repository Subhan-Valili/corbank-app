package az.corbank.mscorbank.domain.model;

import java.time.LocalDate;

/** Every field is optional; null means "not restricted". */
public record OperationFilter(
        LocalDate fromDate,
        LocalDate toDate,
        String search,
        OperationDirection direction
) {
    public static OperationFilter none() {
        return new OperationFilter(null, null, null, null);
    }
}
