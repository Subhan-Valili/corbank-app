package az.corbank.abb.adapter.out.abbclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response shape shared by POST /payments, /payments/signed, /payments/otp, /payments/salary(/signed). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AbbPaymentSubmitResponse(Data data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(String batchNumber) {
    }
}
