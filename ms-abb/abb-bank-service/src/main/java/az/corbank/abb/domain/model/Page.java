package az.corbank.abb.domain.model;

import java.util.List;

/** Generic paged result — spec §4.17 is the only paged reference-data endpoint today. */
public record Page<T>(List<T> items, int currentPage, int pageCount, int pageSize, long itemsCount) {
}
