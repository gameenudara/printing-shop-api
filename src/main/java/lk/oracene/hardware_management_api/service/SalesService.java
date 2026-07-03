package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.request.DraftConfirmRequest;
import lk.oracene.hardware_management_api.dto.request.SalesRequest;
import lk.oracene.hardware_management_api.dto.response.SalesResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SalesService {

    SalesResponse createSale(SalesRequest request);

    SalesResponse getSaleById(Long saleId);

    SalesResponse getSaleByInvoiceNumber(String invoiceNumber);

    Page<SalesResponse> getAllSales(Pageable pageable);

    Page<SalesResponse> searchSalesByInvoiceNumber(String invoiceNumber, Pageable pageable);

    Page<SalesResponse> getSalesByCustomer(Long customerId, Pageable pageable);

    Page<SalesResponse> getPendingSalesByCustomer(Long customerId, Pageable pageable);

    void cancelSale(Long saleId);

    SalesResponse createDraft(SalesRequest request);

    SalesResponse updateDraft(Long saleId, SalesRequest request);

    SalesResponse confirmDraft(Long saleId, DraftConfirmRequest request);

    void deleteDraft(Long saleId);

    Page<SalesResponse> getDrafts(Pageable pageable);
}
