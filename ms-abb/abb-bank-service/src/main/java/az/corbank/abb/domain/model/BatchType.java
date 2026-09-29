package az.corbank.abb.domain.model;

/** Which of ABB's 5 payment submission endpoints (spec §4.2-4.4, §4.11-4.12) a batch went through. */
public enum BatchType {
    REGULAR,
    SIGNED,
    OTP,
    SALARY,
    SALARY_SIGNED;

    public boolean isSalary() {
        return this == SALARY || this == SALARY_SIGNED;
    }
}
