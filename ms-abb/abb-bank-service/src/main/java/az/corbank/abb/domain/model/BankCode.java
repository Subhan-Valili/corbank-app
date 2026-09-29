package az.corbank.abb.domain.model;

/** spec §4.16. */
public record BankCode(String bankCode, String bankName, String swiftAddress) {
}
