package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.oracene.hardware_management_api.dto.request.SupplierBillItemUpdateRequest;
import lk.oracene.hardware_management_api.dto.request.SupplierBillRequest;
import lk.oracene.hardware_management_api.dto.response.OutstandingResponse;
import lk.oracene.hardware_management_api.dto.response.PagedResponse;
import lk.oracene.hardware_management_api.dto.response.SupplierBillItemResponse;
import lk.oracene.hardware_management_api.dto.response.SupplierBillResponse;
import lk.oracene.hardware_management_api.dto.response.SupplierBillSummaryResponse;
import lk.oracene.hardware_management_api.service.SupplierBillService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/supplier-bills")
@RequiredArgsConstructor
@Tag(name = "Supplier Bill", description = "Supplier bill management APIs")
public class SupplierBillController {

    private final SupplierBillService supplierBillService;

    @GetMapping("/all")
    @Operation(summary = "Get all supplier bills ordered by bill date descending (paginated)")
    public ResponseEntity<PagedResponse<SupplierBillSummaryResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(supplierBillService.getAllBills(
                PageRequest.of(page, size, Sort.by("billDate").descending()))));
    }

    @PostMapping
    @Operation(summary = "Create a new supplier bill and update product stock")
    public ResponseEntity<SupplierBillResponse> create(@Valid @RequestBody SupplierBillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierBillService.createBill(request));
    }

    @GetMapping("/{supplierBillId}")
    @Operation(summary = "Get a supplier bill by ID")
    public ResponseEntity<SupplierBillResponse> getById(@PathVariable Long supplierBillId) {
        return ResponseEntity.ok(supplierBillService.getBillById(supplierBillId));
    }

    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "Get all bills for a specific supplier (paginated)")
    public ResponseEntity<PagedResponse<SupplierBillResponse>> getBySupplier(
            @PathVariable Long supplierId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(supplierBillService.getBillsBySupplier(supplierId, PageRequest.of(page, size, Sort.by("billDate").ascending()))));
    }

    @GetMapping("/supplier/{supplierId}/unpaid")
    @Operation(summary = "Get all unpaid bills for a specific supplier (paginated)")
    public ResponseEntity<PagedResponse<SupplierBillResponse>> getUnpaidBillsBySupplier(
            @PathVariable Long supplierId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(supplierBillService.getUnpaidBillsBySupplier(
                supplierId, PageRequest.of(page, size, Sort.by("billDate").ascending()))));
    }

    @GetMapping("/supplier/{supplierId}/outstanding")
    @Operation(summary = "Get total outstanding amount for a specific supplier")
    public ResponseEntity<OutstandingResponse> getOutstandingBySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(supplierBillService.getOutstandingBySupplier(supplierId));
    }

    @GetMapping("/{supplierBillId}/items")
    @Operation(summary = "Get all items of a supplier bill (paginated)")
    public ResponseEntity<PagedResponse<SupplierBillItemResponse>> getItems(
            @PathVariable Long supplierBillId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(supplierBillService.getItemsByBill(
                supplierBillId, PageRequest.of(page, size, Sort.by("supplierBillItemId").ascending()))));
    }

    @PatchMapping("/items/{supplierBillItemId}")
    @Operation(summary = "Update quantity or unit cost price of a bill item")
    public ResponseEntity<SupplierBillItemResponse> updateItem(
            @PathVariable Long supplierBillItemId,
            @Valid @RequestBody SupplierBillItemUpdateRequest request) {
        return ResponseEntity.ok(supplierBillService.updateItem(supplierBillItemId, request));
    }

    @PatchMapping("/{supplierBillId}/cancel")
    @Operation(summary = "Cancel a supplier bill and reverse product stock")
    public ResponseEntity<Void> cancel(@PathVariable Long supplierBillId) {
        supplierBillService.cancelBill(supplierBillId);
        return ResponseEntity.noContent().build();
    }
}
