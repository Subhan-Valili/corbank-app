package az.corbank.mscorbank.domain.model;

/** LIVE = the bank really accepted/processed it; MOCK = rejected or served by a stub. */
public enum PaymentSource {
    LIVE, MOCK
}
