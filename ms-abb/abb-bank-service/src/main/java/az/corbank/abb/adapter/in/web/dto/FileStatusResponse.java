package az.corbank.abb.adapter.in.web.dto;

import az.corbank.abb.domain.model.FileStatus;

public record FileStatusResponse(String externalReference, String batchNumber, String status, String description) {
    public static FileStatusResponse from(FileStatus f) {
        return new FileStatusResponse(f.externalReference(), f.batchNumber(),
                f.status() != null ? f.status().name() : null, f.description());
    }
}
