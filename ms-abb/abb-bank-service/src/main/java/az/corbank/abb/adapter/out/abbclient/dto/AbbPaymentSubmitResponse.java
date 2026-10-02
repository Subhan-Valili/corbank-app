package az.corbank.abb.adapter.out.abbclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response of POST /payments/ — spec §4.3, e.g. {"data": {"batchNumber": "..."}}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AbbPaymentSubmitResponse(Data data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(String batchNumber) {
    }
}
