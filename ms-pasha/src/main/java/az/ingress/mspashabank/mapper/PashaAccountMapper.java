package az.ingress.mspashabank.mapper;

import az.ingress.mspashabank.dto.account.PashaAccountResponseDto;
import az.ingress.mspashabank.entity.PashaAccountEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PashaAccountMapper {
    PashaAccountResponseDto toDto(PashaAccountEntity pashaAccountEntity);

    List<PashaAccountResponseDto> toDto(List<PashaAccountEntity> pashaAccountEntityList);
}
