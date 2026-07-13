package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.oracene.hardware_management_api.dto.request.CashTransactionRequest;
import lk.oracene.hardware_management_api.dto.request.OpenDrawerRequest;
import lk.oracene.hardware_management_api.dto.response.CashDrawerSessionResponse;
import lk.oracene.hardware_management_api.dto.response.PagedResponse;
import lk.oracene.hardware_management_api.service.CashDrawerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cash-drawer")
@RequiredArgsConstructor
@Tag(name = "Cash Drawer", description = "Cash drawer session and transaction tracking APIs")
public class CashDrawerController {

    private final CashDrawerService cashDrawerService;

    @PostMapping("/open")
    @Operation(summary = "Open a new cash drawer session with a starting float")
    public ResponseEntity<CashDrawerSessionResponse> open(@Valid @RequestBody OpenDrawerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cashDrawerService.openDrawer(request));
    }

    @GetMapping("/current")
    @Operation(summary = "Get the current cash drawer session with its live balance and transactions")
    public ResponseEntity<CashDrawerSessionResponse> current() {
        return ResponseEntity.ok(cashDrawerService.getCurrentSession());
    }

    @PostMapping("/cash-in")
    @Operation(summary = "Record a manual cash-in (e.g. change top-up)")
    public ResponseEntity<CashDrawerSessionResponse> cashIn(@Valid @RequestBody CashTransactionRequest request) {
        return ResponseEntity.ok(cashDrawerService.addCashIn(request));
    }

    @PostMapping("/cash-out")
    @Operation(summary = "Record a manual cash-out (e.g. petty cash, cash drop to safe)")
    public ResponseEntity<CashDrawerSessionResponse> cashOut(@Valid @RequestBody CashTransactionRequest request) {
        return ResponseEntity.ok(cashDrawerService.addCashOut(request));
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get cash drawer session history (paginated)")
    public ResponseEntity<PagedResponse<CashDrawerSessionResponse>> sessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                cashDrawerService.getSessionHistory(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))));
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get a specific cash drawer session with its full transaction history")
    public ResponseEntity<CashDrawerSessionResponse> sessionDetail(@PathVariable Long sessionId) {
        return ResponseEntity.ok(cashDrawerService.getSessionDetail(sessionId));
    }
}
