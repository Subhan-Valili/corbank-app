package az.corbank.abb.adapter.out.abbclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** GET /payments/filestatus?external-reference= — spec §4.6. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AbbFileStatusResponse(
        String externalReference,
        String batchNumber,
        StatusDetail status
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatusDetail(String status, String description) {
    }
}
