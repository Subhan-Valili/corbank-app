package az.ingress.mspashabank.mapper;

import az.ingress.mspashabank.dto.account.PashaAccountResponseDto;
import az.ingress.mspashabank.entity.PashaAccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PashaAccountMapper {

    @Mapping(target = "customerNo", source = "customer.customerNo")
    PashaAccountResponseDto toDto(PashaAccountEntity pashaAccountEntity);

    List<PashaAccountResponseDto> toDto(List<PashaAccountEntity> pashaAccountEntityList);
}