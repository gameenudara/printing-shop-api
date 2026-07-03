package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.oracene.hardware_management_api.dto.request.CustomerReturnRequest;
import lk.oracene.hardware_management_api.dto.response.CustomerReturnResponse;
import lk.oracene.hardware_management_api.dto.response.PagedResponse;
import lk.oracene.hardware_management_api.service.CustomerReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer-returns")
@RequiredArgsConstructor
@Tag(name = "Customer Returns", description = "Customer return management APIs")
public class CustomerReturnController {

    private final CustomerReturnService customerReturnService;

    @PostMapping
    @Operation(summary = "Process a customer return")
    public ResponseEntity<CustomerReturnResponse> create(@Valid @RequestBody CustomerReturnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerReturnService.create(request));
    }

    @GetMapping("/{returnId}")
    @Operation(summary = "Get a customer return by ID")
    public ResponseEntity<CustomerReturnResponse> getById(@PathVariable Long returnId) {
        return ResponseEntity.ok(customerReturnService.getById(returnId));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all customer returns (paginated)")
    public ResponseEntity<PagedResponse<CustomerReturnResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(customerReturnService.getAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "returnDate")))));
    }

    @GetMapping("/sale/{saleId}")
    @Operation(summary = "Get all returns for a specific sale")
    public ResponseEntity<List<CustomerReturnResponse>> getBySaleId(@PathVariable Long saleId) {
        return ResponseEntity.ok(customerReturnService.getBySaleId(saleId));
    }
}
