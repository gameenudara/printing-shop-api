package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.response.ChequeResponse;
import lk.oracene.hardware_management_api.model.ChequeStatus;
import lk.oracene.hardware_management_api.model.ChequeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChequeService {

    ChequeResponse getChequeById(Long chequeId);

    Page<ChequeResponse> getAllCheques(Pageable pageable);

    Page<ChequeResponse> getChequesByType(ChequeType chequeType, Pageable pageable);

    Page<ChequeResponse> getChequesByStatus(ChequeStatus chequeStatus, Pageable pageable);

    Page<ChequeResponse> getChequesByCustomer(Long customerId, Pageable pageable);

    Page<ChequeResponse> getPendingChequesByCustomer(Long customerId, Pageable pageable);

    Page<ChequeResponse> getChequesBySupplier(Long supplierId, Pageable pageable);

    Page<ChequeResponse> getPendingChequesBySupplier(Long supplierId, Pageable pageable);

    ChequeResponse getChequeByNumber(String chequeNumber);

    ChequeResponse markAsReturned(Long chequeId);

    ChequeResponse markAsCancelled(Long chequeId);
}
