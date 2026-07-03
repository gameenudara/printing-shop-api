package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.oracene.hardware_management_api.dto.request.ExpenseRequest;
import lk.oracene.hardware_management_api.dto.response.ExpenseDateRangeResponse;
import lk.oracene.hardware_management_api.dto.response.ExpensePagedResponse;
import lk.oracene.hardware_management_api.dto.response.ExpenseResponse;
import lk.oracene.hardware_management_api.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Shop expense management APIs")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @Operation(summary = "Record a new expense")
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.createExpense(request));
    }

    @GetMapping("/{expenseId}")
    @Operation(summary = "Get expense by ID")
    public ResponseEntity<ExpenseResponse> getById(@PathVariable Long expenseId) {
        return ResponseEntity.ok(expenseService.getExpenseById(expenseId));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all expenses (paginated) with total count and total amount")
    public ResponseEntity<ExpensePagedResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(expenseService.getAllExpenses(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "expenseDate"))));
    }

    @GetMapping("/from-date/to-date")
    @Operation(summary = "Get expenses between two dates with total count and total amount")
    public ResponseEntity<ExpenseDateRangeResponse> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return ResponseEntity.ok(expenseService.getExpensesByDateRange(fromDate, toDate));
    }

    @DeleteMapping("/{expenseId}")
    @Operation(summary = "Delete an expense")
    public ResponseEntity<Void> delete(@PathVariable Long expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }
}
