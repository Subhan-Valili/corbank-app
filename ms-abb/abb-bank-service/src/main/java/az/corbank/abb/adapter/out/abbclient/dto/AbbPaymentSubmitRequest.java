package az.corbank.abb.adapter.out.abbclient.dto;

/** Request body shared by POST /payments, /payments/signed, /payments/otp, /payments/salary(/signed). */
public record AbbPaymentSubmitRequest(String base64aDoc, String externalReference) {
}
