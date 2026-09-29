package az.ingress.mspashabank.mapper;

import az.ingress.mspashabank.dto.account.AccountOperationDto;
import az.ingress.mspashabank.entity.AccountOperationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountOperationMapper {

    // ===== DTO -> Entity =====

    @Mapping(target = "externalId", source = "id")
    @Mapping(target = "amountValue", source = "amount.value")
    @Mapping(target = "amountCurrencyCode", source = "amount.currencyCode")
    @Mapping(target = "counterpartyId", source = "counterparty.id")
    @Mapping(target = "counterpartyName", source = "counterparty.name")
    @Mapping(target = "operationDate", source = "date")
    @Mapping(target = "status", constant = "COMPLETED")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "pashaAccount", ignore = true)
    AccountOperationEntity toEntity(AccountOperationDto dto);

    // Siyahı üçün - MapStruct yuxarıdakı toEntity metodunu avtomatik hər elementə tətbiq edir
    List<AccountOperationEntity> toEntityList(List<AccountOperationDto> dtoList);


    // ===== Entity -> DTO =====

    @Mapping(target = "id", source = "externalId")
    @Mapping(target = "date", source = "operationDate")
    @Mapping(target = "amount", expression = "java(toAmountDto(entity))")
    @Mapping(target = "counterparty", expression = "java(toCounterpartyDto(entity))")
    AccountOperationDto toDto(AccountOperationEntity entity);

    // Siyahı üçün - MapStruct yuxarıdakı toDto metodunu avtomatik hər elementə tətbiq edir
    List<AccountOperationDto> toDtoList(List<AccountOperationEntity> entityList);


    // ===== Köməkçi metodlar (nested record-ları qurmaq üçün) =====

    default AccountOperationDto.AmountDto toAmountDto(AccountOperationEntity entity) {
        return new AccountOperationDto.AmountDto(
                entity.getAmountCurrencyCode(),
                entity.getAmountValue()
        );
    }

    default AccountOperationDto.CounterpartyDto toCounterpartyDto(AccountOperationEntity entity) {
        return new AccountOperationDto.CounterpartyDto(
                entity.getCounterpartyId(),
                entity.getCounterpartyName()
        );
    }
}