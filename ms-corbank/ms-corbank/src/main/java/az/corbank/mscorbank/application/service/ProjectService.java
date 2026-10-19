package az.corbank.mscorbank.application.service;

import az.corbank.mscorbank.domain.model.Project;
import az.corbank.mscorbank.application.port.in.GetProjectUseCase;
import az.corbank.mscorbank.application.port.out.ProjectPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class ProjectService implements GetProjectUseCase {

    private final ProjectPort projects;

    ProjectService(ProjectPort projects) {
        this.projects = projects;
    }

    @Override
    public Optional<Project> getProject(String projectId) {
        return projects.findById(projectId);
    }
}
