package az.corbank.abb.application.port.in;

import az.corbank.abb.domain.model.FileStatus;

public interface GetFileStatusUseCase {
    /** spec §4.6. */
    FileStatus getFileStatus(String externalReference);
}
