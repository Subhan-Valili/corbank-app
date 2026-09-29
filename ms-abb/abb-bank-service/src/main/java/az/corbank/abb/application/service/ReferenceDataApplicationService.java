package az.corbank.abb.application.service;

import az.corbank.abb.application.port.in.ReferenceDataUseCase;
import az.corbank.abb.application.port.out.AbbBankGateway;
import az.corbank.abb.domain.model.*;

import java.util.List;

public class ReferenceDataApplicationService implements ReferenceDataUseCase {

    private final AbbBankGateway gateway;

    public ReferenceDataApplicationService(AbbBankGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public List<BudgetType> getBudgetTypes() {
        return gateway.getBudgetTypes();
    }

    @Override
    public List<BudgetCode> getBudgetCodes() {
        return gateway.getBudgetCodes();
    }

    @Override
    public List<BankCode> getBankCodes() {
        return gateway.getBankCodes();
    }

    @Override
    public Page<ForeignBankCode> getForeignBankCodes(int pageNumber, int pageSize) {
        return gateway.getForeignBankCodes(pageNumber, pageSize);
    }

    @Override
    public List<CurrencyRate> getCurrencyRates() {
        return gateway.getCurrencyRates();
    }

    @Override
    public List<SwiftTrackingEntry> getSwiftTracking(String referenceId) {
        return gateway.getSwiftTracking(referenceId);
    }

    @Override
    public void sendSwiftFile(String accountNumber, String refNumber, String fileName, byte[] zipBytes) {
        gateway.sendSwiftFile(accountNumber, refNumber, fileName, zipBytes);
    }

    @Override
    public byte[] getDebitAdviceByRrn(String rrn) {
        return gateway.getDebitAdviceByRrn(rrn);
    }
}
