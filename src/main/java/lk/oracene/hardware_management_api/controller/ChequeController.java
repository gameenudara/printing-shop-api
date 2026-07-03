package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.oracene.hardware_management_api.dto.response.ChequeResponse;
import lk.oracene.hardware_management_api.dto.response.PagedResponse;
import lk.oracene.hardware_management_api.model.ChequeStatus;
import lk.oracene.hardware_management_api.model.ChequeType;
import lk.oracene.hardware_management_api.service.ChequeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cheques")
@RequiredArgsConstructor
@Tag(name = "Cheque", description = "Cheque management APIs")
public class ChequeController {

    private final ChequeService chequeService;

    @GetMapping("/{chequeId}")
    @Operation(summary = "Get a cheque by ID")
    public ResponseEntity<ChequeResponse> getById(@PathVariable Long chequeId) {
        return ResponseEntity.ok(chequeService.getChequeById(chequeId));
    }

    @GetMapping
    @Operation(summary = "Get all cheques (paginated)")
    public ResponseEntity<PagedResponse<ChequeResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(chequeService.getAllCheques(PageRequest.of(page, size))));
    }

    @GetMapping("/type/{chequeType}")
    @Operation(summary = "Get cheques by type (RECEIVED_FROM_CUSTOMER or GIVEN_TO_SUPPLIER)")
    public ResponseEntity<PagedResponse<ChequeResponse>> getByType(
            @PathVariable ChequeType chequeType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                chequeService.getChequesByType(chequeType, PageRequest.of(page, size))));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all cheques received from a specific customer (paginated)")
    public ResponseEntity<PagedResponse<ChequeResponse>> getByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                chequeService.getChequesByCustomer(customerId, PageRequest.of(page, size))));
    }

    @GetMapping("/customer/{customerId}/pending")
    @Operation(summary = "Get all pending cheques for a specific customer (paginated)")
    public ResponseEntity<PagedResponse<ChequeResponse>> getPendingByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                chequeService.getPendingChequesByCustomer(customerId, PageRequest.of(page, size))));
    }

    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "Get all cheques given to a specific supplier (paginated)")
    public ResponseEntity<PagedResponse<ChequeResponse>> getBySupplier(
            @PathVariable Long supplierId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                chequeService.getChequesBySupplier(supplierId, PageRequest.of(page, size))));
    }

    @GetMapping("/supplier/{supplierId}/pending")
    @Operation(summary = "Get all pending cheques for a specific supplier (paginated)")
    public ResponseEntity<PagedResponse<ChequeResponse>> getPendingBySupplier(
            @PathVariable Long supplierId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                chequeService.getPendingChequesBySupplier(supplierId, PageRequest.of(page, size))));
    }

    @GetMapping("/search")
    @Operation(summary = "Search cheque by cheque number")
    public ResponseEntity<ChequeResponse> getByNumber(@RequestParam String chequeNumber) {
        return ResponseEntity.ok(chequeService.getChequeByNumber(chequeNumber));
    }

    @GetMapping("/status/{chequeStatus}")
    @Operation(summary = "Get cheques by status (PENDING, CLEARED, RETURNED, CANCELLED)")
    public ResponseEntity<PagedResponse<ChequeResponse>> getByStatus(
            @PathVariable ChequeStatus chequeStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                chequeService.getChequesByStatus(chequeStatus, PageRequest.of(page, size))));
    }

    @PatchMapping("/{chequeId}/return")
    @Operation(summary = "Mark a cheque as returned")
    public ResponseEntity<ChequeResponse> markAsReturned(@PathVariable Long chequeId) {
        return ResponseEntity.ok(chequeService.markAsReturned(chequeId));
    }

    @PatchMapping("/{chequeId}/cancel")
    @Operation(summary = "Mark a cheque as cancelled")
    public ResponseEntity<ChequeResponse> markAsCancelled(@PathVariable Long chequeId) {
        return ResponseEntity.ok(chequeService.markAsCancelled(chequeId));
    }

    @GetMapping("/types")
    @Operation(summary = "Get all cheque types for dropdown")
    public ResponseEntity<List<String>> getChequeTypes() {
        return ResponseEntity.ok(Arrays.stream(ChequeType.values()).map(Enum::name).toList());
    }

    @GetMapping("/statuses")
    @Operation(summary = "Get all cheque statuses for dropdown")
    public ResponseEntity<List<String>> getChequeStatuses() {
        return ResponseEntity.ok(Arrays.stream(ChequeStatus.values()).map(Enum::name).toList());
    }
}
