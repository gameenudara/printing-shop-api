package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.response.CustomerOutstandingReportResponse;
import lk.oracene.hardware_management_api.dto.response.CustomerOutstandingReportResponse.CustomerOutstandingRow;
import lk.oracene.hardware_management_api.model.Customer;
import lk.oracene.hardware_management_api.repository.PaymentRepository;
import lk.oracene.hardware_management_api.repository.SalesRepository;
import lk.oracene.hardware_management_api.service.CustomerReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerReportServiceImpl implements CustomerReportService {

    private final SalesRepository salesRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public CustomerOutstandingReportResponse getOutstandingReport() {
        List<Customer> customers = salesRepository.findDistinctCustomersWithPendingSales();

        List<CustomerOutstandingRow> rows = customers.stream()
                .map(customer -> {
                    BigDecimal totalBill = salesRepository.sumTotalAmountByCustomerId(customer.getCustomerId());
                    BigDecimal paid = paymentRepository.sumPaidAmountByCustomerId(customer.getCustomerId());
                    BigDecimal outstanding = totalBill.subtract(paid);

                    LocalDateTime oldestPendingDate = salesRepository.findOldestPendingSaleDateByCustomerId(customer.getCustomerId());
                    long daysPending = oldestPendingDate != null
                            ? ChronoUnit.DAYS.between(oldestPendingDate.toLocalDate(), LocalDateTime.now().toLocalDate())
                            : 0;

                    return CustomerOutstandingRow.builder()
                            .customerId(customer.getCustomerId())
                            .customerName(customer.getCustomerName())
                            .phone(customer.getPhone())
                            .totalBillAmount(totalBill)
                            .paidAmount(paid)
                            .outstandingAmount(outstanding)
                            .oldestPendingDate(oldestPendingDate)
                            .daysPending(daysPending)
                            .build();
                })
                .filter(row -> row.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getOutstandingAmount().compareTo(a.getOutstandingAmount()))
                .toList();

        BigDecimal totalOutstanding = rows.stream()
                .map(CustomerOutstandingRow::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CustomerOutstandingReportResponse.builder()
                .totalCustomers(rows.size())
                .totalOutstanding(totalOutstanding)
                .customers(rows)
                .build();
    }
}
