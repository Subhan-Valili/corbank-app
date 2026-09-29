package az.ingress.mspashabank.specification;

import az.ingress.mspashabank.entity.AccountOperationEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class AccountOperationSpecifications {

    private AccountOperationSpecifications() {
    }

    public static Specification<AccountOperationEntity> hasAccountId(Long accountId) {
        return (root, query, cb) ->
                cb.equal(root.get("pashaAccount").get("id"), accountId);
    }

    public static Specification<AccountOperationEntity> operationDateFrom(LocalDateTime fromDate) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("operationDate"), fromDate);
    }

    public static Specification<AccountOperationEntity> operationDateTo(LocalDateTime toDate) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("operationDate"), toDate);
    }

    public static Specification<AccountOperationEntity> amountValueFrom(BigDecimal from) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("amountValue"), from);
    }

    public static Specification<AccountOperationEntity> amountValueTo(BigDecimal to) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("amountValue"), to);
    }

    public static Specification<AccountOperationEntity> searchValueLike(String searchValue) {
        String pattern = "%" + searchValue.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("description")), pattern),
                cb.like(cb.lower(root.get("counterpartyName")), pattern)
        );
    }
}