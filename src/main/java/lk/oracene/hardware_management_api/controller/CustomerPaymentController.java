package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.oracene.hardware_management_api.dto.request.CustomerPaymentRequest;
import lk.oracene.hardware_management_api.dto.response.CustomerPaymentResponse;
import lk.oracene.hardware_management_api.dto.response.PagedResponse;
import lk.oracene.hardware_management_api.service.CustomerPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer-payments")
@RequiredArgsConstructor
@Tag(name = "Customer Payment", description = "Customer payment management APIs")
public class CustomerPaymentController {

    private final CustomerPaymentService customerPaymentService;

    @PostMapping
    @Operation(summary = "Add a payment for a customer sale")
    public ResponseEntity<CustomerPaymentResponse> addPayment(
            @Valid @RequestBody CustomerPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerPaymentService.addPayment(request));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all payments for a customer (paginated)")
    public ResponseEntity<PagedResponse<CustomerPaymentResponse>> getByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                customerPaymentService.getPaymentsByCustomer(customerId, PageRequest.of(page, size))));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get a payment by ID")
    public ResponseEntity<CustomerPaymentResponse> getById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(customerPaymentService.getPaymentById(paymentId));
    }
}
