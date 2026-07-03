package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.oracene.hardware_management_api.dto.request.CustomerRequest;
import lk.oracene.hardware_management_api.dto.response.CustomerResponse;
import lk.oracene.hardware_management_api.dto.response.OutstandingResponse;
import lk.oracene.hardware_management_api.dto.response.PagedResponse;
import lk.oracene.hardware_management_api.dto.response.SalesResponse;
import lk.oracene.hardware_management_api.service.CustomerService;
import lk.oracene.hardware_management_api.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer management APIs")
public class CustomerController {

    private final CustomerService customerService;
    private final SalesService salesService;

    @PostMapping
    @Operation(summary = "Create a new customer")
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse created = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/update/{customerId}")
    @Operation(summary = "Update an existing customer")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(customerId, request));
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get a customer by ID")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getCustomerById(customerId));
    }

    @GetMapping("/search")
    @Operation(summary = "Search active customers by name (case-insensitive, partial match)")
    public ResponseEntity<PagedResponse<CustomerResponse>> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                customerService.searchCustomersByName(name, PageRequest.of(page, size))));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active customers (paginated)")
    public ResponseEntity<PagedResponse<CustomerResponse>> getAllActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(customerService.getAllActiveCustomers(PageRequest.of(page, size))));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all customers including inactive ones (paginated)")
    public ResponseEntity<PagedResponse<CustomerResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(customerService.getAllCustomers(PageRequest.of(page, size))));
    }

    @GetMapping("/{customerId}/pending-bills")
    @Operation(summary = "Get all pending bills for a specific customer, oldest first (paginated)")
    public ResponseEntity<PagedResponse<SalesResponse>> getPendingBills(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(salesService.getPendingSalesByCustomer(customerId,
                PageRequest.of(page, size, Sort.by("saleDate").ascending()))));
    }

    @GetMapping("/{customerId}/outstanding")
    @Operation(summary = "Get total outstanding amount for a specific customer")
    public ResponseEntity<OutstandingResponse> getOutstandingByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getOutstandingByCustomer(customerId));
    }

    @DeleteMapping("/delete/{customerId}")
    @Operation(summary = "Deactivate a customer (sets isActive to false)")
    public ResponseEntity<Void> deactivate(@PathVariable Long customerId) {
        customerService.deactivateCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{customerId}/activate")
    @Operation(summary = "Reactivate an inactive customer")
    public ResponseEntity<Void> activate(@PathVariable Long customerId) {
        customerService.activateCustomer(customerId);
        return ResponseEntity.noContent().build();
    }
}
