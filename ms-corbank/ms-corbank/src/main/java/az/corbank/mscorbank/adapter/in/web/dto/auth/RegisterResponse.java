package az.corbank.mscorbank.adapter.in.web.dto.auth;

public record RegisterResponse(
        String signUpSessionId,
        String message
) {}