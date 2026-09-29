package az.corbank.mscorbank.application.port.in;

import az.corbank.mscorbank.domain.model.Project;

import java.util.Optional;

public interface GetProjectUseCase {

    Optional<Project> getProject(String projectId);
}
