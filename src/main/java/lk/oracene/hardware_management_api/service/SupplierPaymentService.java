package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.request.SupplierPaymentRequest;
import lk.oracene.hardware_management_api.dto.response.SupplierPaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierPaymentService {

    SupplierPaymentResponse recordPayment(SupplierPaymentRequest request);

    Page<SupplierPaymentResponse> getPaymentsByBill(Long supplierBillId, Pageable pageable);
}
