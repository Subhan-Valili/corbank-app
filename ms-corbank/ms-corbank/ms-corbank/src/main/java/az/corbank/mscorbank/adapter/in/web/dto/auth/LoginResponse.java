package az.corbank.mscorbank.adapter.in.web.dto.auth;

public record LoginResponse(
        String accessToken,
        String refreshToken
)
{}