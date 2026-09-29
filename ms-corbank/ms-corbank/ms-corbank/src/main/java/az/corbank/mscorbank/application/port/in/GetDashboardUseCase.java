package az.corbank.mscorbank.application.port.in;

import az.corbank.mscorbank.domain.model.Bank;
import az.corbank.mscorbank.domain.model.Dashboard;

public interface GetDashboardUseCase {

    /** selectedBank == null (or a bank that is not connected) means all banks. */
    Dashboard getDashboard(Bank selectedBank);
}
