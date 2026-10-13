package az.corbank.abb.adapter.out.abbclient.dto;

/** Request body of POST /payments/ — spec §4.3. base64aDoc is the Payment Order XML (§5.1), base64-encoded. */
public record AbbPaymentSubmitRequest(String base64aDoc, String externalReference) {
}
