package az.corbank.mscorbank.adapter.in.web.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Ad boş ola bilməz")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Soyad boş ola bilməz")
        @Size(max = 100)
        String surname,

        @NotBlank
        @Email(message = "Email düzgün deyil")
        String email,

        @NotBlank
        @Size(min = 8, max = 64, message = "Parol 8-64 simvol olmalıdır")
        String password,

        @NotBlank
        @Size(max = 20)
        String mobileNumber,

        @NotBlank
        String pinCode
) {}
