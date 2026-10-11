package az.corbank.mscorbank.adapter.out.persistence.repository.jpa;

import az.corbank.mscorbank.adapter.out.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    @Query("select r from RoleEntity r where r.name = :name")
    Optional<RoleEntity> findByName(@Param(value = "name") String name);

}
