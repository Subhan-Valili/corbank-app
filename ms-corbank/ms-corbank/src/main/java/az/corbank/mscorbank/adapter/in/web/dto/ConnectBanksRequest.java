package az.corbank.mscorbank.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/** "Finish" sorğusu: Open API Key-i olan hər bank üçün bir element. */
public record ConnectBanksRequest(
        @NotEmpty(message = "Ən azı bir bank üçün Open API Key daxil edin")
        List<@Valid Item> connections
) {
    public record Item(
            @NotBlank(message = "Bank seçilməyib")
            String bank,

            @NotBlank(message = "Open API Key boş ola bilməz")
            @Size(max = 500, message = "Open API Key çox uzundur")
            String apiKey
    ) {}
}
