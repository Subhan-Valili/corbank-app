package az.ingress.mspashabank.service.impl;

import az.ingress.mspashabank.dto.gpp.*;
import az.ingress.mspashabank.entity.GppChildInvoiceEntity;
import az.ingress.mspashabank.entity.GppInvoiceEntity;
import az.ingress.mspashabank.entity.GppPaymentEntity;
import az.ingress.mspashabank.enums.FeeCalculationMethod;
import az.ingress.mspashabank.repository.GppPaymentRepository;
import az.ingress.mspashabank.service.GppPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GppPaymentServiceImpl implements GppPaymentService {

    private final GppPaymentRepository gppPaymentRepository;

    @Override
    @Transactional
    public GppPaymentResponseDto createPayment(GppPaymentRequestDto request) {
        log.info("Creating GPP payment: {}", request);

        GppPaymentEntity paymentEntity = buildPaymentEntity(request);

        paymentEntity.setStatus("CREATED");
        paymentEntity.setBankStatusCode("200");
        paymentEntity.setCreateDate(LocalDateTime.now());
        paymentEntity.setExternalPaymentId(System.currentTimeMillis()); // Bankadan dönen unikal ID simülasyonu

        BigDecimal totalTransferAmount = request.getInvoices() != null ?
                request.getInvoices().stream()
                        .map(InvoiceDto::getAmountDue)
                        .filter(a -> a != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;

        BigDecimal totalCommission = request.getInvoices() != null ?
                request.getInvoices().stream()
                        .map(InvoiceDto::getCommissionAmount)
                        .filter(c -> c != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;

        paymentEntity.setTransferAmount(totalTransferAmount);
        paymentEntity.setCommissionAmount(totalCommission);

        GppPaymentEntity savedEntity = gppPaymentRepository.save(paymentEntity);

        return buildResponseDto(savedEntity, request);
    }

    private GppPaymentEntity buildPaymentEntity(GppPaymentRequestDto request) {
        GppPaymentEntity.GppPaymentEntityBuilder<?, ?> builder = GppPaymentEntity.builder()
                .description(request.getDescription());

        if (request.getPayer() != null) {
            builder.payerAccountNumber(request.getPayer().getAccountNumber())
                    .payerTin(request.getPayer().getTin())
                    .payerType(request.getPayer().getType());
        }

        if (request.getInvoiceRequest() != null) {
            builder.invoiceRequestCode(request.getInvoiceRequest().getCode())
                    .invoiceRequestPrefix(request.getInvoiceRequest().getPrefix())
                    .invoiceRequestAdditionalCode(request.getInvoiceRequest().getAdditionalCode())
                    .invoiceRequestIdentificationSubtype(request.getInvoiceRequest().getIdentificationSubtype())
                    .invoiceRequestScCode(request.getInvoiceRequest().getScCode())
                    .invoiceRequestSpCode(request.getInvoiceRequest().getSpCode())
                    .invoiceRequestServiceCodeList(request.getInvoiceRequest().getServiceCodeList());
        }

        GppPaymentEntity payment = builder.build();

        if (request.getInvoices() != null) {
            for (InvoiceDto invDto : request.getInvoices()) {
                GppInvoiceEntity invoiceEntity = buildInvoiceEntity(invDto);
                payment.addInvoice(invoiceEntity);
            }
        }

        return payment;
    }

    private GppInvoiceEntity buildInvoiceEntity(InvoiceDto dto) {
        List<GppInvoiceEntity.PaymentReceiverEmbeddable> receivers = dto.getPaymentReceivers() == null ?
                Collections.emptyList() :
                dto.getPaymentReceivers().stream()
                        .map(r -> new GppInvoiceEntity.PaymentReceiverEmbeddable(r.getCode(), r.getDescription()))
                        .toList();

        GppInvoiceEntity invoiceEntity = GppInvoiceEntity.builder()
                .externalInvoiceId(dto.getId())
                .invoiceCode(dto.getInvoiceCode())
                .invoiceType(dto.getInvoiceType())
                .stateCode(dto.getStateCode())
                .receiptNumber(dto.getReceiptNumber())
                .feeCalculationMethod(dto.getFeeCalculationMethod())
                .amountDue(dto.getAmountDue())
                .commissionAmount(dto.getCommissionAmount())
                .currentDebt(dto.getCurrentDebt())
                .maxAllowedAmount(dto.getMaxAllowedAmount())
                .minAllowedAmount(dto.getMinAllowedAmount())
                .partialPaymentAllowed(dto.getPartialPaymentAllowed())
                .manualPaymentReceiverSelectionRequired(dto.getManualPaymentReceiverSelectionRequired())
                .manualSpBranchSelectionRequired(dto.getManualSpBranchSelectionRequired())
                .serviceCode(dto.getServiceCode())
                .serviceDescription(dto.getServiceDescription())
                .spBranchCode(dto.getSpBranchCode())
                .spBranchDescription(dto.getSpBranchDescription())
                .paymentReceiverCode(dto.getPaymentReceiverCode())
                .paymentReceiverDescription(dto.getPaymentReceiverDescription())
                .paymentReceivers(receivers)
                .build();

        if (dto.getChildInvoiceList() != null) {
            for (ChildInvoiceDto childDto : dto.getChildInvoiceList()) {
                GppChildInvoiceEntity childEntity = GppChildInvoiceEntity.builder()
                        .externalChildInvoiceId(childDto.getId())
                        .invoiceCode(childDto.getInvoiceCode())
                        .receiptNumber(childDto.getReceiptNumber())
                        .feeCalculationMethod(childDto.getFeeCalculationMethod())
                        .amount(childDto.getAmount())
                        .commissionAmount(childDto.getCommissionAmount())
                        .serviceCode(childDto.getServiceCode())
                        .serviceDescription(childDto.getServiceDescription())
                        .paymentReceiverCode(childDto.getPaymentReceiverCode())
                        .paymentReceiverDescription(childDto.getPaymentReceiverDescription())
                        .build();

                invoiceEntity.addChildInvoice(childEntity);
            }
        }

        return invoiceEntity;
    }

    private GppPaymentResponseDto buildResponseDto(GppPaymentEntity entity, GppPaymentRequestDto request) {
        return GppPaymentResponseDto.builder()
                .id(entity.getExternalPaymentId())
                .bankStatusCode(entity.getBankStatusCode())
                .commissionAmount(entity.getCommissionAmount())
                .createDate(entity.getCreateDate())
                .description(entity.getDescription())
                .invoiceRequest(request.getInvoiceRequest())
                .invoices(request.getInvoices())
                .payer(request.getPayer())
                .rejectReason(entity.getRejectReason())
                .status(entity.getStatus())
                .transferAmount(entity.getTransferAmount())
                .type(entity.getType())
                .build();
    }

    @Override
    public CommissionResponseDto calculateCommission(CalculateCommissionRequestDto request) {
        BigDecimal amount = request.getAmount();
        FeeCalculationMethod method = request.getFeeCalculationMethod();

        BigDecimal commission = switch (method) {
            case WITHOUT_FEE -> BigDecimal.ZERO;
            case GPP_FEE -> amount.multiply(BigDecimal.valueOf(0.01)); // 1%
            case SP_FIXED_FEE -> BigDecimal.valueOf(1.00); // Sabit 1 AZN
            case ACQUIRER_FEE -> amount.multiply(BigDecimal.valueOf(0.015)); // 1.5%
            case PSP_FEE -> amount.multiply(BigDecimal.valueOf(0.005)); // 0.5%
        };

        return CommissionResponseDto.builder()
                .commission(commission.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    @Override
    public GetInvoiceInfoResponseDto getInvoiceInfo(InvoiceRequestDto request) {
        InvoiceInfoDto mockInvoice = InvoiceInfoDto.builder()
                .id("INV-2026-001")
                .invoiceType("SINGLE")
                .feeCalculationMethod(FeeCalculationMethod.WITHOUT_FEE)
                .currentDebt(BigDecimal.valueOf(350.00))
                .commissionAmount(BigDecimal.ZERO)
                .minAllowedAmount(BigDecimal.valueOf(1.00))
                .maxAllowedAmount(BigDecimal.valueOf(10000.00))
                .partialPaymentAllowed(true)
                .serviceCode(request.getServiceCodeList() != null && !request.getServiceCodeList().isEmpty()
                        ? request.getServiceCodeList().get(0) : 101)
                .serviceDescription("Mənfəət və Gəlir Vergisi Ödənişi")
                .paymentReceiverCode(5001)
                .paymentReceiverDescription("Dövlət Vergi Xidməti")
                .paymentReceivers(List.of(
                        PaymentReceiverDto.builder()
                                .code(5001)
                                .description("Dövlət Vergi Xidməti")
                                .build()
                ))
                .build();

        PrePaidServiceGroupDto mockPrePaidGroup = PrePaidServiceGroupDto.builder()
                .description("Vergi və Rüsum Xidmətləri")
                .prePaidServices(List.of(
                        PrePaidServiceDto.builder()
                                .code(101)
                                .description("Fiziki Şəxslərin Gəlir Vergisi")
                                .build()
                ))
                .build();

        return GetInvoiceInfoResponseDto.builder()
                .invoices(List.of(mockInvoice))
                .prePaidServiceGroups(List.of(mockPrePaidGroup))
                .build();
    }

    @Override
    public List<MerchantResponseDto> getMerchants() {
        return List.of(
                MerchantResponseDto.builder()
                        .code(1001)
                        .displayName("Dövlət Vergi Xidməti")
                        .name("STATE_TAX_SERVICE")
                        .identifiers(List.of(
                                MerchantIdentifierDto.builder()
                                        .identificationSubtype("AVIS")
                                        .subtype("TIN")
                                        .build()
                        ))
                        .build(),
                MerchantResponseDto.builder()
                        .code(1002)
                        .displayName("Daxili İşlər Nazirliyi (YPX)")
                        .name("MIA")
                        .identifiers(List.of(
                                MerchantIdentifierDto.builder()
                                        .identificationSubtype("AVIS")
                                        .subtype("SERIAL")
                                        .build()
                        ))
                        .build(),
                MerchantResponseDto.builder()
                        .code(1003)
                        .displayName("Yerli Bələdiyyələr")
                        .name("MUNICIPALITIES")
                        .identifiers(List.of(
                                MerchantIdentifierDto.builder()
                                        .identificationSubtype("AVIS")
                                        .subtype("TIN")
                                        .build()
                        ))
                        .build()
        );
    }
}