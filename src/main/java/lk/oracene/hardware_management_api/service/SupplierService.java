package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.request.SupplierRequest;
import lk.oracene.hardware_management_api.dto.response.SupplierResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierService {

    SupplierResponse createSupplier(SupplierRequest request);

    SupplierResponse updateSupplier(Long supplierId, SupplierRequest request);

    SupplierResponse getSupplierById(Long supplierId);

    Page<SupplierResponse> getAllActiveSuppliers(Pageable pageable);

    Page<SupplierResponse> getAllSuppliers(Pageable pageable);

    void deactivateSupplier(Long supplierId);

    void activateSupplier(Long supplierId);
}