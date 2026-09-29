package az.ingress.mspashabank.mapper;

import az.ingress.mspashabank.dto.bulk.CreateBulkPaymentRequest;
import az.ingress.mspashabank.dto.bulk.CreateBulkPaymentResponse;
import az.ingress.mspashabank.dto.bulk.GetBulkIdResponse;
import az.ingress.mspashabank.entity.B2bBulkPaymentEntity;
import az.ingress.mspashabank.entity.B2bPaymentItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface B2bPaymentMapper {

    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    B2bBulkPaymentEntity toBulkEntity(CreateBulkPaymentRequest request);

    @Mapping(source = "payer.accountNumber", target = "payerAccountNumber")
    @Mapping(source = "payee.accountNumber", target = "payeeAccountNumber")
    @Mapping(source = "payee.name", target = "payeeName")
    @Mapping(source = "payee.tin", target = "payeeTin")
    @Mapping(source = "payee.type", target = "payeeType")
    @Mapping(source = "payee.email", target = "payeeEmail")
    @Mapping(source = "payee.address", target = "payeeAddress")
    @Mapping(source = "payee.additionalInfo", target = "payeeAdditionalInfo")
    @Mapping(source = "payee.bank.code", target = "payeeBankCode")
    @Mapping(source = "payee.bank.name", target = "payeeBankName")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    B2bPaymentItemEntity toItemEntity(CreateBulkPaymentRequest.PaymentRequest request);

    @Mapping(source = "bulkId", target = "bulkId")
    @Mapping(source = "bulkDescription", target = "description")
    @Mapping(source = "recordCount", target = "recordCount")
    CreateBulkPaymentResponse toCreateResponse(B2bBulkPaymentEntity entity);

    @Mapping(source = "entity.bulkId", target = "bulkId")
    @Mapping(source = "referenceNumber", target = "referenceNumber")
    GetBulkIdResponse toGetBulkIdResponse(B2bBulkPaymentEntity entity, String referenceNumber);
}