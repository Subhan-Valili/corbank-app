package az.corbank.abb.adapter.out.abbclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AbbBudgetTypeDto(String bdgtype, String bdgtypeName) {
}
