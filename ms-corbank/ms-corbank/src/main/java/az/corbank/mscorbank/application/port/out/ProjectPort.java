package az.corbank.mscorbank.application.port.out;

import az.corbank.mscorbank.domain.model.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectPort {

    List<Project> findAll();

    Optional<Project> findById(String id);
}
