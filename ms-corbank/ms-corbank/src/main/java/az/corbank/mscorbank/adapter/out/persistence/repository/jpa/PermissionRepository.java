package az.corbank.mscorbank.adapter.out.persistence.repository.jpa;

import az.corbank.mscorbank.adapter.out.persistence.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity,Long> {
}
