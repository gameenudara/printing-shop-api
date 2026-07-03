package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.oracene.hardware_management_api.dto.request.SupplierReturnRequest;
import lk.oracene.hardware_management_api.dto.response.PagedResponse;
import lk.oracene.hardware_management_api.dto.response.SupplierReturnResponse;
import lk.oracene.hardware_management_api.service.SupplierReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/supplier-returns")
@RequiredArgsConstructor
@Tag(name = "Supplier Returns", description = "Supplier return management APIs")
public class SupplierReturnController {

    private final SupplierReturnService supplierReturnService;

    @PostMapping
    @Operation(summary = "Create a manual supplier return")
    public ResponseEntity<SupplierReturnResponse> create(@Valid @RequestBody SupplierReturnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierReturnService.create(request));
    }

    @GetMapping("/{supplierReturnId}")
    @Operation(summary = "Get a supplier return by ID")
    public ResponseEntity<SupplierReturnResponse> getById(@PathVariable Long supplierReturnId) {
        return ResponseEntity.ok(supplierReturnService.getById(supplierReturnId));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all supplier returns (paginated)")
    public ResponseEntity<PagedResponse<SupplierReturnResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(supplierReturnService.getAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "returnDate")))));
    }
}
