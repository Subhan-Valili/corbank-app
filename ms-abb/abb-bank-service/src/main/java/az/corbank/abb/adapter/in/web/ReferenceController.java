package az.corbank.abb.adapter.in.web;

import az.corbank.abb.application.port.in.ReferenceDataUseCase;
import az.corbank.abb.domain.model.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * These reference-data endpoints serialize domain records straight onto the wire rather
 * than going through a dedicated web DTO per type. That's a deliberate, pragmatic call:
 * unlike accounts/payments, these are plain flat lookup values with no behavior and no
 * alternate representation the web layer needs to shape — wrapping each in an identical
 * pass-through record would add files without adding any real decoupling.
 */
@RestController
@RequestMapping("/internal/abb")
class ReferenceController {

    private final ReferenceDataUseCase referenceData;

    ReferenceController(ReferenceDataUseCase referenceData) {
        this.referenceData = referenceData;
    }

    @GetMapping("/budget-types")
    List<BudgetType> getBudgetTypes() {
        return referenceData.getBudgetTypes();
    }

    @GetMapping("/budget-codes")
    List<BudgetCode> getBudgetCodes() {
        return referenceData.getBudgetCodes();
    }

    @GetMapping("/bank-codes")
    List<BankCode> getBankCodes() {
        return referenceData.getBankCodes();
    }

    @GetMapping("/foreign-bank-codes")
    Page<ForeignBankCode> getForeignBankCodes(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "50") int pageSize) {
        return referenceData.getForeignBankCodes(pageNumber, pageSize);
    }

    @GetMapping("/currency-rates")
    List<CurrencyRate> getCurrencyRates() {
        return referenceData.getCurrencyRates();
    }

    @GetMapping("/swift/track")
    List<SwiftTrackingEntry> getSwiftTracking(@RequestParam String referenceId) {
        return referenceData.getSwiftTracking(referenceId);
    }

    /** POST /internal/abb/swift/files — multipart upload, forwarded to ABB as-is (spec §4.19). */
    @PostMapping(value = "/swift/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void sendSwiftFile(@RequestParam String accountNumber,
                        @RequestParam(required = false) String refNumber,
                        @RequestParam MultipartFile file) {
        try {
            referenceData.sendSwiftFile(accountNumber, refNumber, file.getOriginalFilename(), file.getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** GET /internal/abb/debit-advice?rrn= — streams the PDF back as-is (spec §4.22). */
    @GetMapping("/debit-advice")
    ResponseEntity<byte[]> getDebitAdvice(@RequestParam String rrn) {
        byte[] pdf = referenceData.getDebitAdviceByRrn(rrn);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"debit-advice-" + rrn + ".pdf\"")
                .body(pdf);
    }
}
