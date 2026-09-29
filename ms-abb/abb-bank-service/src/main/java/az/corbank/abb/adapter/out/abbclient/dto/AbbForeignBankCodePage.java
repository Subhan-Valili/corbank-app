package az.corbank.abb.adapter.out.abbclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AbbForeignBankCodePage(
        long itemsCount,
        int currentPage,
        int pageCount,
        int pageSize,
        List<Item> items
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(String bicCode, String bankName) {
    }
}
