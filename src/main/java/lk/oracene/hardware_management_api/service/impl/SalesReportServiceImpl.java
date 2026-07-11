package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.response.CategorySalesReportResponse;
import lk.oracene.hardware_management_api.dto.response.CategorySalesReportResponse.CategorySalesRow;
import lk.oracene.hardware_management_api.dto.response.FinancialSummaryResponse;
import lk.oracene.hardware_management_api.dto.response.ProfitLossResponse;
import lk.oracene.hardware_management_api.dto.response.SalesReportResponse;
import lk.oracene.hardware_management_api.dto.response.SalesResponse;
import lk.oracene.hardware_management_api.model.Payment;
import lk.oracene.hardware_management_api.model.PaymentStatus;
import lk.oracene.hardware_management_api.model.Sales;
import lk.oracene.hardware_management_api.model.SalesItem;
import lk.oracene.hardware_management_api.model.SalesStatus;
import lk.oracene.hardware_management_api.repository.ExpenseRepository;
import lk.oracene.hardware_management_api.repository.PaymentRepository;
import lk.oracene.hardware_management_api.repository.SalesItemRepository;
import lk.oracene.hardware_management_api.repository.SalesRepository;
import lk.oracene.hardware_management_api.service.SalesReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesReportServiceImpl implements SalesReportService {

    private final SalesRepository salesRepository;
    private final SalesItemRepository salesItemRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public SalesReportResponse getSalesReport(LocalDateTime fromDate, LocalDateTime toDate) {
        List<Sales> sales = salesRepository.findBySaleDateBetweenOrderBySaleDateAsc(fromDate, toDate);

        List<Sales> countableSales = sales.stream()
                .filter(s -> s.getStatus() == SalesStatus.UNPAID
                        || s.getStatus() == SalesStatus.ADVANCE_PAID
                        || s.getStatus() == SalesStatus.PAID)
                .toList();

        BigDecimal totalRevenue = countableSales.stream()
                .map(Sales::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscount = countableSales.stream()
                .map(Sales::getDiscountAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SalesResponse> salesResponses = sales.stream()
                .map(this::buildSalesResponse)
                .toList();

        return SalesReportResponse.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .totalSalesCount(countableSales.size())
                .totalRevenue(totalRevenue)
                .totalDiscountAmount(totalDiscount)
                .sales(salesResponses)
                .build();
    }

    @Override
    public FinancialSummaryResponse getTodayFinancialSummary() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.of(23, 59, 59));

        List<Sales> sales = salesRepository.findBySaleDateBetweenOrderBySaleDateAsc(startOfDay, endOfDay)
                .stream()
                .filter(s -> s.getStatus() == SalesStatus.UNPAID
                        || s.getStatus() == SalesStatus.ADVANCE_PAID
                        || s.getStatus() == SalesStatus.PAID)
                .toList();

        BigDecimal revenue = sales.stream()
                .map(Sales::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Long> saleIds = sales.stream().map(Sales::getSalesId).toList();

        BigDecimal costOfRevenue = salesItemRepository.findBySale_SalesIdIn(saleIds).stream()
                .map(item -> {
                    BigDecimal cost = item.getProduct().getCostPrice();
                    return (cost != null) ? cost.multiply(item.getQuantity()) : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal grossProfit = revenue.subtract(costOfRevenue).setScale(2, RoundingMode.HALF_UP);

        BigDecimal expenses = expenseRepository.sumAmountsByDateRange(startOfDay, endOfDay)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal netProfit = grossProfit.subtract(expenses).setScale(2, RoundingMode.HALF_UP);

        BigDecimal profitMargin = revenue.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(revenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return FinancialSummaryResponse.builder()
                .date(LocalDate.now())
                .revenue(revenue)
                .costOfRevenue(costOfRevenue)
                .grossProfit(grossProfit)
                .expenses(expenses)
                .netProfit(netProfit)
                .profitMargin(profitMargin)
                .build();
    }

    @Override
    public CategorySalesReportResponse getCategorySalesReport(LocalDateTime fromDate, LocalDateTime toDate) {
        List<SalesStatus> countableStatuses = List.of(
                SalesStatus.UNPAID, SalesStatus.ADVANCE_PAID, SalesStatus.PAID);

        List<SalesItem> items = salesItemRepository.findBySaleDateRangeAndStatuses(fromDate, toDate, countableStatuses);

        Map<Long, List<SalesItem>> byCategory = items.stream()
                .collect(Collectors.groupingBy(i -> i.getProduct().getCategory().getCategoryId()));

        List<CategorySalesRow> rows = byCategory.entrySet().stream()
                .map(entry -> {
                    List<SalesItem> categoryItems = entry.getValue();
                    BigDecimal totalQty = categoryItems.stream()
                            .map(SalesItem::getQuantity)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalRevenue = categoryItems.stream()
                            .map(SalesItem::getLineTotal)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .setScale(2, RoundingMode.HALF_UP);
                    String categoryName = categoryItems.get(0).getProduct().getCategory().getName();
                    return CategorySalesRow.builder()
                            .categoryId(entry.getKey())
                            .categoryName(categoryName)
                            .quantitySold(totalQty)
                            .revenue(totalRevenue)
                            .build();
                })
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .toList();

        return CategorySalesReportResponse.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .categories(rows)
                .build();
    }

    @Override
    public ProfitLossResponse getProfitLoss(LocalDateTime fromDate, LocalDateTime toDate) {
        List<SalesStatus> countableStatuses = List.of(
                SalesStatus.UNPAID, SalesStatus.ADVANCE_PAID, SalesStatus.PAID);

        List<Sales> sales = salesRepository.findBySaleDateBetweenOrderBySaleDateAsc(fromDate, toDate)
                .stream()
                .filter(s -> countableStatuses.contains(s.getStatus()))
                .toList();

        BigDecimal revenue = sales.stream()
                .map(Sales::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Long> saleIds = sales.stream().map(Sales::getSalesId).toList();

        BigDecimal costOfRevenue = salesItemRepository.findBySale_SalesIdIn(saleIds).stream()
                .map(item -> {
                    BigDecimal cost = item.getProduct().getCostPrice();
                    return cost != null ? cost.multiply(item.getQuantity()) : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal grossProfit = revenue.subtract(costOfRevenue).setScale(2, RoundingMode.HALF_UP);

        BigDecimal expenses = expenseRepository.sumAmountsByDateRange(fromDate, toDate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal netProfit = grossProfit.subtract(expenses).setScale(2, RoundingMode.HALF_UP);

        String type = netProfit.compareTo(BigDecimal.ZERO) >= 0 ? "PROFIT" : "LOSS";

        BigDecimal profitMargin = revenue.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(revenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return ProfitLossResponse.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .revenue(revenue)
                .costOfRevenue(costOfRevenue)
                .grossProfit(grossProfit)
                .expenses(expenses)
                .netProfit(netProfit)
                .type(type)
                .profitMargin(profitMargin.abs())
                .build();
    }

    private SalesResponse buildSalesResponse(Sales sale) {
        BigDecimal totalPaid = paymentRepository.findBySale_SalesId(sale.getSalesId())
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getPaidAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = sale.getTotalAmount() != null
                ? sale.getTotalAmount().subtract(totalPaid)
                : BigDecimal.ZERO;

        return SalesResponse.builder()
                .salesId(sale.getSalesId())
                .invoiceNumber(sale.getInvoiceNumber())
                .customerId(sale.getCustomer() != null ? sale.getCustomer().getCustomerId() : null)
                .customerName(sale.getCustomer() != null ? sale.getCustomer().getCustomerName() : null)
                .saleDate(sale.getSaleDate())
                .subTotal(sale.getSubTotal())
                .discountAmount(sale.getDiscountAmount())
                .totalAmount(sale.getTotalAmount())
                .paidAmount(totalPaid)
                .remainingAmount(remaining)
                .status(sale.getStatus())
                .build();
    }

}
