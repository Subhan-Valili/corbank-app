package az.corbank.mscorbank.adapter.in.web.dto.auth;

public record PendingRegistration(
        String name,
        String surname,
        String email,
        String passwordHash,
        String mobileNumber,
        String pinCode
) {}
