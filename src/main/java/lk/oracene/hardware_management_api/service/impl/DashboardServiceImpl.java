package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.response.RecentSaleResponse;
import lk.oracene.hardware_management_api.dto.response.RevenueTrendResponse;
import lk.oracene.hardware_management_api.dto.response.SalesByCategoryResponse;
import lk.oracene.hardware_management_api.dto.response.TodaySummaryResponse;
import lk.oracene.hardware_management_api.dto.response.TopSellingProductsResponse;
import lk.oracene.hardware_management_api.model.Payment;
import lk.oracene.hardware_management_api.model.Sales;
import lk.oracene.hardware_management_api.model.SalesItem;
import lk.oracene.hardware_management_api.model.SalesStatus;
import lk.oracene.hardware_management_api.repository.PaymentRepository;
import lk.oracene.hardware_management_api.repository.SalesItemRepository;
import lk.oracene.hardware_management_api.repository.SalesRepository;
import lk.oracene.hardware_management_api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final int TREND_DAYS = 30;
    private static final List<SalesStatus> EXCLUDED_STATUSES = List.of(
            SalesStatus.CANCELLED, SalesStatus.REFUNDED, SalesStatus.DRAFT);
    private static final List<SalesStatus> ACTIVE_STATUSES = List.of(
            SalesStatus.PENDING, SalesStatus.PARTIAL, SalesStatus.COMPLETED, SalesStatus.PARTIAL_REFUND);

    private final SalesRepository salesRepository;
    private final SalesItemRepository salesItemRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public RevenueTrendResponse getRevenueTrend() {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(TREND_DAYS - 1);

        List<Sales> sales = salesRepository.findBySaleDateBetweenOrderBySaleDateAsc(
                from.atStartOfDay(),
                today.atTime(LocalTime.MAX));

        Map<LocalDate, BigDecimal> revenueByDate = sales.stream()
                .filter(s -> !EXCLUDED_STATUSES.contains(s.getStatus()))
                .collect(Collectors.groupingBy(
                        s -> s.getSaleDate().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Sales::getTotalAmount, BigDecimal::add)));

        List<String> labels = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();

        for (int i = 0; i < TREND_DAYS; i++) {
            LocalDate date = from.plusDays(i);
            labels.add(date.toString());
            values.add(revenueByDate.getOrDefault(date, BigDecimal.ZERO));
        }

        BigDecimal totalRevenue = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageRevenue = totalRevenue.divide(BigDecimal.valueOf(TREND_DAYS), 2, RoundingMode.HALF_UP);
        BigDecimal highestRevenue = values.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal lowestRevenue = values.stream()
                .filter(v -> v.compareTo(BigDecimal.ZERO) > 0)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return RevenueTrendResponse.builder()
                .totalRevenue(totalRevenue)
                .averageRevenue(averageRevenue)
                .highestRevenue(highestRevenue)
                .lowestRevenue(lowestRevenue)
                .labels(labels)
                .values(values)
                .build();
    }

    @Override
    public TodaySummaryResponse getTodaySummary() {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.atTime(LocalTime.MAX);

        Long totalOrders = salesRepository.countByDateRangeExcludingStatuses(from, to, EXCLUDED_STATUSES);
        BigDecimal totalSales = salesRepository.sumRevenueByDateRangeExcludingStatuses(from, to, EXCLUDED_STATUSES);
        BigDecimal totalMoney = paymentRepository.sumSuccessfulPaymentsBetween(from, to);

        List<SalesItem> items = salesItemRepository.findBySaleDateRangeAndStatuses(from, to, ACTIVE_STATUSES);
        BigDecimal totalProfit = items.stream()
                .map(item -> {
                    BigDecimal costPrice = item.getProduct().getCostPrice() != null
                            ? item.getProduct().getCostPrice() : BigDecimal.ZERO;
                    return item.getLineTotal().subtract(costPrice.multiply(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return TodaySummaryResponse.builder()
                .totalOrders(totalOrders)
                .totalSales(totalSales)
                .totalMoney(totalMoney)
                .totalProfit(totalProfit)
                .build();
    }

    @Override
    public SalesByCategoryResponse getSalesByCategory() {
        List<Object[]> rows = salesItemRepository.findSalesByCategoryExcludingStatuses(EXCLUDED_STATUSES);

        List<String> labels = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();

        for (Object[] row : rows) {
            labels.add((String) row[0]);
            values.add(((BigDecimal) row[1]).setScale(2, RoundingMode.HALF_UP));
        }

        BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        List<BigDecimal> percentages = values.stream()
                .map(v -> total.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                        : v.multiply(BigDecimal.valueOf(100))
                                .divide(total, 2, RoundingMode.HALF_UP))
                .toList();

        return SalesByCategoryResponse.builder()
                .labels(labels)
                .values(values)
                .percentages(percentages)
                .total(total)
                .build();
    }

    @Override
    public TopSellingProductsResponse getTopSellingProducts(int limit) {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.minusDays(TREND_DAYS - 1).atStartOfDay();
        LocalDateTime to = today.atTime(LocalTime.MAX);

        List<Object[]> rows = salesItemRepository.findMostSellingProducts(
                from, to, ACTIVE_STATUSES, PageRequest.of(0, limit)).getContent();

        List<String> labels = new ArrayList<>();
        List<BigDecimal> quantities = new ArrayList<>();
        List<BigDecimal> revenues = new ArrayList<>();

        for (Object[] row : rows) {
            labels.add((String) row[1]);
            quantities.add(((BigDecimal) row[2]).setScale(2, RoundingMode.HALF_UP));
            revenues.add(((BigDecimal) row[3]).setScale(2, RoundingMode.HALF_UP));
        }

        return TopSellingProductsResponse.builder()
                .labels(labels)
                .quantities(quantities)
                .revenues(revenues)
                .build();
    }

    @Override
    public List<RecentSaleResponse> getRecentSales() {
        List<Sales> recentSales = salesRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "saleDate"))).getContent();

        if (recentSales.isEmpty()) return List.of();

        List<Long> saleIds = recentSales.stream().map(Sales::getSalesId).toList();

        Map<Long, lk.oracene.hardware_management_api.model.PaymentMethod> paymentMethodBySaleId =
                paymentRepository.findBySale_SalesIdIn(saleIds).stream()
                        .sorted(Comparator.comparing(Payment::getPaidAt,
                                Comparator.nullsFirst(Comparator.naturalOrder())))
                        .collect(Collectors.toMap(
                                p -> p.getSale().getSalesId(),
                                Payment::getMethod,
                                (existing, replacement) -> replacement));

        return recentSales.stream()
                .map(sale -> RecentSaleResponse.builder()
                        .invoiceNumber(sale.getInvoiceNumber())
                        .customerName(sale.getCustomer() != null
                                ? sale.getCustomer().getCustomerName() : "Walk-in")
                        .amount(sale.getTotalAmount())
                        .paymentMethod(paymentMethodBySaleId.get(sale.getSalesId()))
                        .status(sale.getStatus())
                        .saleDate(sale.getSaleDate())
                        .build())
                .toList();
    }
}
