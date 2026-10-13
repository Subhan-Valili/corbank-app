package az.corbank.mscorbank.adapter.in.web;

import az.corbank.mscorbank.adapter.in.web.dto.FxRateViewDto;
import az.corbank.mscorbank.application.port.in.GetFxRatesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Login tələb etməyən (guest) endpoint-lər. Burada heç bir şəxsi məlumat olmamalıdır. */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicWebController {

    private final GetFxRatesUseCase getFxRates;

    @GetMapping("/fx-rates")
    public ResponseEntity<List<FxRateViewDto>> fxRates() {
        return ResponseEntity.ok(getFxRates.getRates().stream().map(WebMapper::toDto).toList());
    }
}
