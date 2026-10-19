package az.corbank.mscorbank.application.port.in;

import az.corbank.mscorbank.domain.model.OperationFilter;
import az.corbank.mscorbank.domain.model.OperationsReport;

public interface ListOperationsUseCase {

    /** Never throws because a bank is down: an unreachable bank simply contributes no operations. */
    OperationsReport listOperations(String accountId, OperationFilter filter);
}
