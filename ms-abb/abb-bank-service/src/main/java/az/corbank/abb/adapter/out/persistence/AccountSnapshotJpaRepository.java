package az.corbank.abb.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface AccountSnapshotJpaRepository extends JpaRepository<AccountSnapshotJpaEntity, String> {
}
