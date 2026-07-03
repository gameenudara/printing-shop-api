package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.request.SupplierBillItemUpdateRequest;
import lk.oracene.hardware_management_api.dto.request.SupplierBillRequest;
import lk.oracene.hardware_management_api.dto.response.OutstandingResponse;
import lk.oracene.hardware_management_api.dto.response.SupplierBillItemResponse;
import lk.oracene.hardware_management_api.dto.response.SupplierBillResponse;
import lk.oracene.hardware_management_api.dto.response.SupplierBillSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface SupplierBillService {

    SupplierBillResponse createBill(SupplierBillRequest request);

    SupplierBillResponse getBillById(Long supplierBillId);

    Page<SupplierBillSummaryResponse> getAllBills(Pageable pageable);

    Page<SupplierBillItemResponse> getItemsByBill(Long supplierBillId, Pageable pageable);

    SupplierBillItemResponse updateItem(Long supplierBillItemId, SupplierBillItemUpdateRequest request);

    Page<SupplierBillResponse> getBillsBySupplier(Long supplierId, Pageable pageable);

    Page<SupplierBillResponse> getUnpaidBillsBySupplier(Long supplierId, Pageable pageable);

    OutstandingResponse getOutstandingBySupplier(Long supplierId);

    void cancelBill(Long supplierBillId);
}
