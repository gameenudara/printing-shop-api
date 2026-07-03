package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.oracene.hardware_management_api.dto.request.SupplierPaymentRequest;
import lk.oracene.hardware_management_api.dto.response.BankNameResponse;
import lk.oracene.hardware_management_api.dto.response.PagedResponse;
import lk.oracene.hardware_management_api.dto.response.SupplierPaymentResponse;
import lk.oracene.hardware_management_api.model.BankName;
import lk.oracene.hardware_management_api.service.SupplierPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/supplier-payments")
@RequiredArgsConstructor
@Tag(name = "Supplier Payment", description = "Supplier payment management APIs")
public class SupplierPaymentController {

    private final SupplierPaymentService supplierPaymentService;

    @PostMapping
    @Operation(summary = "Record a payment against a supplier bill")
    public ResponseEntity<SupplierPaymentResponse> record(@Valid @RequestBody SupplierPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierPaymentService.recordPayment(request));
    }

    @GetMapping("/banks")
    @Operation(summary = "Get all bank names for dropdown")
    public ResponseEntity<List<BankNameResponse>> getBankNames() {
        List<BankNameResponse> banks = Arrays.stream(BankName.values())
                .map(bank -> BankNameResponse.builder()
                        .value(bank.name())
                        .label(bank.getLabel())
                        .build())
                .toList();
        return ResponseEntity.ok(banks);
    }

    @GetMapping("/bill/{supplierBillId}")
    @Operation(summary = "Get all payments for a specific supplier bill (paginated)")
    public ResponseEntity<PagedResponse<SupplierPaymentResponse>> getByBill(
            @PathVariable Long supplierBillId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(supplierPaymentService.getPaymentsByBill(supplierBillId, PageRequest.of(page, size))));
    }
}
