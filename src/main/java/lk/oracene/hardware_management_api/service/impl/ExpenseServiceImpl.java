package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.request.ExpenseRequest;
import lk.oracene.hardware_management_api.dto.response.ExpenseDateRangeResponse;
import lk.oracene.hardware_management_api.dto.response.ExpensePagedResponse;
import lk.oracene.hardware_management_api.dto.response.ExpenseResponse;
import lk.oracene.hardware_management_api.exception.NotFoundException;
import lk.oracene.hardware_management_api.model.Expense;
import lk.oracene.hardware_management_api.repository.ExpenseRepository;
import lk.oracene.hardware_management_api.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Override
    public ExpenseResponse createExpense(ExpenseRequest request) {
        Expense expense = new Expense();
        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setNote(request.getNote());
        return mapToResponse(expenseRepository.save(expense));
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long expenseId) {
        return mapToResponse(findById(expenseId));
    }

    @Override
    @Transactional(readOnly = true)
    public ExpensePagedResponse getAllExpenses(Pageable pageable) {
        Page<Expense> page = expenseRepository.findAllByOrderByExpenseDateDesc(pageable);
        return ExpensePagedResponse.builder()
                .totalExpenseCount(expenseRepository.count())
                .totalExpenseAmount(expenseRepository.sumAllAmounts())
                .content(page.getContent().stream().map(this::mapToResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDateRangeResponse getExpensesByDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        List<ExpenseResponse> expenses = expenseRepository
                .findByExpenseDateBetweenOrderByExpenseDateAsc(fromDate, toDate)
                .stream()
                .map(this::mapToResponse)
                .toList();
        return ExpenseDateRangeResponse.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .totalExpenseCount(expenses.size())
                .totalExpenseAmount(expenseRepository.sumAmountsByDateRange(fromDate, toDate))
                .expenses(expenses)
                .build();
    }

    @Override
    public void deleteExpense(Long expenseId) {
        expenseRepository.delete(findById(expenseId));
    }

    private Expense findById(Long expenseId) {
        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found with id: " + expenseId));
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        return ExpenseResponse.builder()
                .expenseId(expense.getExpenseId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .note(expense.getNote())
                .build();
    }
}
