package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.request.ExpenseRequest;
import lk.oracene.hardware_management_api.dto.response.ExpenseDateRangeResponse;
import lk.oracene.hardware_management_api.dto.response.ExpensePagedResponse;
import lk.oracene.hardware_management_api.dto.response.ExpenseResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ExpenseService {

    ExpenseResponse createExpense(ExpenseRequest request);

    ExpenseResponse getExpenseById(Long expenseId);

    ExpensePagedResponse getAllExpenses(Pageable pageable);

    ExpenseDateRangeResponse getExpensesByDateRange(LocalDateTime fromDate, LocalDateTime toDate);

    void deleteExpense(Long expenseId);
}
