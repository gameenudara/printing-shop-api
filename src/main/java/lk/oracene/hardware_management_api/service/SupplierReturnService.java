package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.request.SupplierReturnRequest;
import lk.oracene.hardware_management_api.dto.response.SupplierReturnResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface SupplierReturnService {
    SupplierReturnResponse create(SupplierReturnRequest request);
    SupplierReturnResponse getById(Long supplierReturnId);
    Page<SupplierReturnResponse> getAll(Pageable pageable);
    Page<SupplierReturnResponse> getByDateRange(LocalDateTime from, LocalDateTime to, Pageable pageable);
}
