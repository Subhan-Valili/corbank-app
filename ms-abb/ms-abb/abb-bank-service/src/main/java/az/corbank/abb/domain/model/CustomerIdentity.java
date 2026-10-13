package az.corbank.abb.domain.model;

import az.corbank.abb.domain.exception.InvalidCustomerIdentityException;

/**
 * Who ms-corbank is asking on behalf of. Individuals are identified by PIN (FIN), corporate
 * customers have no PIN and are identified by mobile number. If both are supplied, both
 * must match the same customer.
 *
 * The mobile number is normalized to E.164 (+994...) so "050 111 22 33", "0501112233" and
 * "+994501112233" all resolve to the same customer. toString() masks both values so the
 * identity never leaks into logs.
 */
public record CustomerIdentity(String pin, String mobileNumber) {

    public CustomerIdentity {
        pin = (pin == null || pin.isBlank()) ? null : pin.trim();
        mobileNumber = normalizeMobile(mobileNumber);
        if (pin == null && mobileNumber == null) {
            throw new InvalidCustomerIdentityException("Either PIN or mobile number is required.");
        }
    }

    static String normalizeMobile(String raw) {
        if (raw == null) return null;
        String s = raw.replaceAll("[\\s\\-()]", "");
        if (s.isEmpty()) return null;
        if (s.startsWith("+")) return s;
        if (s.startsWith("00")) return "+" + s.substring(2);
        if (s.startsWith("994")) return "+" + s;
        if (s.startsWith("0")) return "+994" + s.substring(1);
        return "+994" + s;
    }

    @Override
    public String toString() {
        return "CustomerIdentity[pin=" + (pin != null ? "***" : "null")
                + ", mobileNumber=" + (mobileNumber != null ? "***" : "null") + "]";
    }
}
