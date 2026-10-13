package az.corbank.abb.adapter.out.abbclient.dto;

/** Body of POST /payments/auth/token — plain username/password, per spec §4.1. */
public record AbbTokenRequest(String username, String password) {
}
