package az.ingress.mspashabank.service;

import az.ingress.mspashabank.dto.bulk.*;

import java.util.List;

public interface B2bBulkPaymentService {
    CreateBulkPaymentResponse createBulkPayments(CreateBulkPaymentRequest request);
    void approveBulkPayments(ApproveBulkPaymentRequest request);
    GetBulkIdResponse getBulkIdByReferenceNumber(String referenceNumber);
    FxRatesResponse getFxRates();
}
