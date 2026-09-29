package az.corbank.abb.domain.model;

public enum DataOrigin {
    LIVE,               // fetched from ABB just now
    SNAPSHOT_FALLBACK   // ABB was unreachable; served the last persisted snapshot instead
}
