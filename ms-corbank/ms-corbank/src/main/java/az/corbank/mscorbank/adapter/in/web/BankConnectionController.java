package az.corbank.mscorbank.adapter.in.web;

import az.corbank.mscorbank.adapter.in.web.dto.BankStatusDto;
import az.corbank.mscorbank.adapter.in.web.dto.ConnectBanksRequest;
import az.corbank.mscorbank.adapter.in.web.dto.ConnectedBankDto;
import az.corbank.mscorbank.application.exception.BankConnectionException;
import az.corbank.mscorbank.application.port.in.ManageBankConnectionsUseCase;
import az.corbank.mscorbank.application.port.in.ManageBankConnectionsUseCase.ConnectCommand;
import az.corbank.mscorbank.domain.model.Bank;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Onboarding axını: login -> boş ekran (banklar siyahısı) -> hansı bankın Open API Key-i varsa daxil et -> Finish.
 * Finish-dən dərhal sonra /api/dashboard artıq həmin bankların hesab və balanslarını göstərir.
 */
@RestController
@RequestMapping("/api/bank-connections")
@RequiredArgsConstructor
public class BankConnectionController {

    private final ManageBankConnectionsUseCase connections;

    /** Bütün banklar + hansının qoşulu olduğu (boş ekranı çəkmək üçün). */
    @GetMapping
    public ResponseEntity<List<BankStatusDto>> list() {
        return ResponseEntity.ok(connections.listBanks().stream()
                .map(s -> BankStatusDto.builder()
                        .code(s.bank().name())
                        .name(s.bank().displayName())
                        .apiKeySupported(s.apiKeySupported())
                        .connected(s.connected())
                        .build())
                .toList());
    }

    /** "Finish". Hamısı bankdan yoxlanır; biri yanlışdırsa heç biri saxlanmır. */
    @PostMapping
    public ResponseEntity<List<ConnectedBankDto>> connect(@Valid @RequestBody ConnectBanksRequest request) {
        List<ConnectCommand> commands = request.connections().stream()
                .map(item -> new ConnectCommand(
                        Bank.fromCode(item.bank()).orElseThrow(
                                () -> new BankConnectionException("Naməlum bank: " + item.bank())),
                        item.apiKey()))
                .toList();

        return ResponseEntity.ok(connections.connect(commands).stream()
                .map(c -> ConnectedBankDto.builder()
                        .code(c.bank().name())
                        .name(c.bank().displayName())
                        .accountCount(c.accounts().size())
                        .accounts(c.accounts().stream().map(WebMapper::toDto).toList())
                        .build())
                .toList());
    }

    @DeleteMapping("/{bank}")
    public ResponseEntity<Void> disconnect(@PathVariable("bank") String bank) {
        connections.disconnect(Bank.fromCode(bank).orElseThrow(
                () -> new BankConnectionException("Naməlum bank: " + bank)));
        return ResponseEntity.noContent().build();
    }
}
