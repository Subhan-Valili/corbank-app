package az.corbank.abb.adapter.out.abbclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * ABB's error shape, e.g. {"statusCode": "ERR2", "statusDesc": "...", "timeStamp": "..."}.
 * The spec inconsistently capitalizes "timeStamp"/"timestamp" and sometimes adds "path" —
 * all fields are optional here since we mostly just log this and surface statusDesc.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AbbErrorResponse(String statusCode, String statusDesc, String timeStamp, String path) {
}
