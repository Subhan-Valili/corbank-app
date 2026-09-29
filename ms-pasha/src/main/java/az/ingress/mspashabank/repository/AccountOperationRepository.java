package az.ingress.mspashabank.repository;

import az.ingress.mspashabank.entity.AccountOperationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface AccountOperationRepository
        extends JpaRepository<AccountOperationEntity, Long>, JpaSpecificationExecutor<AccountOperationEntity> {

}