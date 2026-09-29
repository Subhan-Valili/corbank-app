package az.corbank.mscorbank.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record Project(
        String id,
        ProjectType type,
        String name,
        BigDecimal amount,
        String currency,
        String meta,
        List<Detail> details
) {
    public record Detail(String label, String value) {
    }
}
