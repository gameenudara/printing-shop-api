package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.oracene.hardware_management_api.dto.request.SupplierRequest;
import lk.oracene.hardware_management_api.dto.response.PagedResponse;
import lk.oracene.hardware_management_api.dto.response.SupplierResponse;
import lk.oracene.hardware_management_api.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Supplier", description = "Supplier management APIs")
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @Operation(summary = "Create a new supplier")
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(request));
    }

    @PutMapping("/update/{supplierId}")
    @Operation(summary = "Update an existing supplier")
    public ResponseEntity<SupplierResponse> update(
            @PathVariable Long supplierId,
            @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplier(supplierId, request));
    }

    @GetMapping("/{supplierId}")
    @Operation(summary = "Get an active supplier by ID")
    public ResponseEntity<SupplierResponse> getById(@PathVariable Long supplierId) {
        return ResponseEntity.ok(supplierService.getSupplierById(supplierId));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active suppliers (paginated)")
    public ResponseEntity<PagedResponse<SupplierResponse>> getAllActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(supplierService.getAllActiveSuppliers(PageRequest.of(page, size))));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all suppliers including inactive ones (paginated)")
    public ResponseEntity<PagedResponse<SupplierResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(supplierService.getAllSuppliers(PageRequest.of(page, size))));
    }

    @DeleteMapping("/delete/{supplierId}")
    @Operation(summary = "Deactivate a supplier (sets isActive to false)")
    public ResponseEntity<Void> deactivate(@PathVariable Long supplierId) {
        supplierService.deactivateSupplier(supplierId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{supplierId}/activate")
    @Operation(summary = "Reactivate an inactive supplier (sets isActive to true)")
    public ResponseEntity<Void> activate(@PathVariable Long supplierId) {
        supplierService.activateSupplier(supplierId);
        return ResponseEntity.noContent().build();
    }
}
