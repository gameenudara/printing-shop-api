package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.oracene.hardware_management_api.dto.request.DraftConfirmRequest;
import lk.oracene.hardware_management_api.dto.request.SalesRequest;
import lk.oracene.hardware_management_api.dto.response.PagedResponse;
import lk.oracene.hardware_management_api.dto.response.SalesResponse;
import lk.oracene.hardware_management_api.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@Tag(name = "Sales", description = "Sales management APIs")
public class SalesController {

    private final SalesService salesService;

    @PostMapping
    @Operation(summary = "Create a new sale")
    public ResponseEntity<SalesResponse> create(@Valid @RequestBody SalesRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salesService.createSale(request));
    }

    @GetMapping("/{saleId}")
    @Operation(summary = "Get a sale by ID")
    public ResponseEntity<SalesResponse> getById(@PathVariable Long saleId) {
        return ResponseEntity.ok(salesService.getSaleById(saleId));
    }

    @GetMapping("/invoice/{invoiceNumber}")
    @Operation(summary = "Get a sale by invoice number")
    public ResponseEntity<SalesResponse> getByInvoiceNumber(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(salesService.getSaleByInvoiceNumber(invoiceNumber));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all sales (paginated)")
    public ResponseEntity<PagedResponse<SalesResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(salesService.getAllSales(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "saleDate")))));
    }

    @GetMapping("/search")
    @Operation(summary = "Search sales by invoice number (partial match, paginated)")
    public ResponseEntity<PagedResponse<SalesResponse>> searchByInvoiceNumber(
            @RequestParam String invoiceNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(salesService.searchSalesByInvoiceNumber(
                invoiceNumber, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "saleDate")))));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all sales for a specific customer (paginated)")
    public ResponseEntity<PagedResponse<SalesResponse>> getByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(salesService.getSalesByCustomer(customerId, PageRequest.of(page, size))));
    }

    @PatchMapping("/{saleId}/cancel")
    @Operation(summary = "Cancel a sale")
    public ResponseEntity<Void> cancel(@PathVariable Long saleId) {
        salesService.cancelSale(saleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/draft")
    @Operation(summary = "Save a new sale as draft")
    public ResponseEntity<SalesResponse> createDraft(@Valid @RequestBody SalesRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salesService.createDraft(request));
    }

    @PutMapping("/draft/{saleId}")
    @Operation(summary = "Update a draft sale")
    public ResponseEntity<SalesResponse> updateDraft(
            @PathVariable Long saleId,
            @Valid @RequestBody SalesRequest request) {
        return ResponseEntity.ok(salesService.updateDraft(saleId, request));
    }

    @PostMapping("/draft/{saleId}/confirm")
    @Operation(summary = "Confirm a draft sale — converts to a real sale with invoice")
    public ResponseEntity<SalesResponse> confirmDraft(
            @PathVariable Long saleId,
            @RequestBody DraftConfirmRequest request) {
        return ResponseEntity.ok(salesService.confirmDraft(saleId, request));
    }

    @DeleteMapping("/draft/{saleId}")
    @Operation(summary = "Delete a draft sale")
    public ResponseEntity<Void> deleteDraft(@PathVariable Long saleId) {
        salesService.deleteDraft(saleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/drafts")
    @Operation(summary = "Get all draft sales (paginated)")
    public ResponseEntity<PagedResponse<SalesResponse>> getDrafts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                salesService.getDrafts(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "saleDate")))));
    }
}
