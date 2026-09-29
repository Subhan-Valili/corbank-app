package az.corbank.abb.domain.model;

/**
 * File-level status codes from spec §3.6, plus SUBMITTED — our own local state for the
 * moment a batch has been accepted but never polled yet. Pure domain vocabulary; knows
 * nothing about how ABB spells these on the wire (that's the adapter's job).
 */
public enum BatchStatusCode {
    SUBMITTED,
    COMPLETED,
    FAILURE,
    IN_PROGRESS,
    PARTIAL,
    ERROR
}
