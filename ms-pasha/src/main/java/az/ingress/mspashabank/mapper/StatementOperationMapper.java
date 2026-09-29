package az.ingress.mspashabank.mapper;

import az.ingress.mspashabank.dto.account.StatementOperationDto;
import az.ingress.mspashabank.entity.AccountOperationEntity;
import az.ingress.mspashabank.entity.PashaAccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StatementOperationMapper {

    @Mapping(target = "accountCurrency", source = "account.currency")
    @Mapping(target = "afterOperationAvlBalance", source = "account.availableBalance")
    @Mapping(target = "afterOperationBalance", source = "account.currentBalance")
    @Mapping(target = "amountInAccountCurrency", source = "entity.amountValue")
    @Mapping(target = "amountInTransactionCurrency", source = "entity.amountValue")
    @Mapping(target = "amountInTransactionCurrencyAzn", source = "entity.amountValue")
    @Mapping(target = "cardNo", ignore = true)
    @Mapping(target = "closingAvlBalance", source = "account.availableBalance")
    @Mapping(target = "closingBalance", source = "account.currentBalance")
    @Mapping(target = "closingBalanceAzn", source = "account.currentBalance")
    @Mapping(target = "counterParty", source = "entity.counterpartyName")
    @Mapping(target = "counterPartyId", source = "entity.counterpartyId")
    @Mapping(target = "counterPartyName", source = "entity.counterpartyName")
    @Mapping(target = "counterPartyPin", ignore = true)
    @Mapping(target = "counterPartyTin", source = "account.tin")
    @Mapping(target = "openingAvlBalance", source = "account.todayOpeningBalance")
    @Mapping(target = "openingBalance", source = "account.todayOpeningBalance")
    @Mapping(target = "operationDate", source = "entity.operationDate")
    @Mapping(target = "sourceSystem", source = "entity.source")
    @Mapping(target = "transactionCurrency", source = "entity.amountCurrencyCode")
    @Mapping(target = "transactionDate", source = "entity.operationDate")
    @Mapping(target = "transactionDescription", source = "entity.description")
    @Mapping(target = "transactionFXRate", expression = "java(java.math.BigDecimal.ONE)")
    @Mapping(target = "transactionNo", source = "entity.externalId")
    @Mapping(target = "transactionType", source = "entity.type")
    StatementOperationDto toDto(AccountOperationEntity entity, PashaAccountEntity account);
}