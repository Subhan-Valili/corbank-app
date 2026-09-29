package az.ingress.mspashabank.service;

import az.ingress.mspashabank.dto.gpp.*;

import java.util.List;

public interface GppPaymentService {
    GppPaymentResponseDto createPayment(GppPaymentRequestDto request);
    CommissionResponseDto calculateCommission(CalculateCommissionRequestDto request);
    GetInvoiceInfoResponseDto getInvoiceInfo(InvoiceRequestDto request);
    List<MerchantResponseDto> getMerchants();
}