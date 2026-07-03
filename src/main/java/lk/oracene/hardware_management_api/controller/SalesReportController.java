package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.oracene.hardware_management_api.dto.response.CategorySalesReportResponse;
import lk.oracene.hardware_management_api.dto.response.CustomerOutstandingReportResponse;
import lk.oracene.hardware_management_api.dto.response.CustomerReturnResponse;
import lk.oracene.hardware_management_api.dto.response.FinancialSummaryResponse;
import lk.oracene.hardware_management_api.dto.response.PagedResponse;
import lk.oracene.hardware_management_api.dto.response.ProfitLossResponse;
import lk.oracene.hardware_management_api.dto.response.SalesReportResponse;
import lk.oracene.hardware_management_api.dto.response.SupplierOutstandingReportResponse;
import lk.oracene.hardware_management_api.dto.response.SupplierReturnResponse;
import lk.oracene.hardware_management_api.service.CustomerReportService;
import lk.oracene.hardware_management_api.service.CustomerReturnService;
import lk.oracene.hardware_management_api.service.SalesReportService;
import lk.oracene.hardware_management_api.service.SupplierReportService;
import lk.oracene.hardware_management_api.service.SupplierReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Report", description = "Report APIs")
public class SalesReportController {

    private final SalesReportService reportService;
    private final CustomerReportService customerReportService;
    private final SupplierReportService supplierReportService;
    private final CustomerReturnService customerReturnService;
    private final SupplierReturnService supplierReturnService;

    @GetMapping("/sales/from-date/to-date")
    @Operation(summary = "Get sales report between two date-times")
    public ResponseEntity<SalesReportResponse> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return ResponseEntity.ok(reportService.getSalesReport(fromDate, toDate));
    }

    @GetMapping("/sales/today")
    @Operation(summary = "Get today's sales report (12:00 AM to 11:59 PM)")
    public ResponseEntity<SalesReportResponse> getTodaySalesReport() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.of(23, 59, 59));
        return ResponseEntity.ok(reportService.getSalesReport(startOfDay, endOfDay));
    }

    @GetMapping("/sales/financial-summary/today")
    @Operation(summary = "Get today's financial summary: Revenue, Expenses, Gross Profit, Profit Margin")
    public ResponseEntity<FinancialSummaryResponse> getTodayFinancialSummary() {
        return ResponseEntity.ok(reportService.getTodayFinancialSummary());
    }

    @GetMapping("/sales/by-category")
    @Operation(summary = "Get sales report grouped by category with quantity sold and revenue")
    public ResponseEntity<CategorySalesReportResponse> getCategorySalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return ResponseEntity.ok(reportService.getCategorySalesReport(fromDate, toDate));
    }

    @GetMapping("/sales/profit-loss")
    @Operation(summary = "Get total profit or loss for a specific time period")
    public ResponseEntity<ProfitLossResponse> getProfitLoss(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return ResponseEntity.ok(reportService.getProfitLoss(fromDate, toDate));
    }

    @GetMapping("/customers/outstanding")
    @Operation(summary = "Get all customers with outstanding balances, sorted by highest outstanding first")
    public ResponseEntity<CustomerOutstandingReportResponse> getCustomerOutstanding() {
        return ResponseEntity.ok(customerReportService.getOutstandingReport());
    }

    @GetMapping("/supplier/outstanding")
    @Operation(summary = "Get all suppliers with outstanding balances, sorted by highest outstanding first")
    public ResponseEntity<SupplierOutstandingReportResponse> getSupplierOutstanding() {
        return ResponseEntity.ok(supplierReportService.getOutstandingReport());
    }

    @GetMapping("/customers/returns")
    @Operation(summary = "Get customer returns between two dates (paginated)")
    public ResponseEntity<PagedResponse<CustomerReturnResponse>> getCustomerReturns(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                customerReturnService.getByDateRange(fromDate, toDate, PageRequest.of(page, size))));
    }

    @GetMapping("/supplier/returns")
    @Operation(summary = "Get supplier returns between two dates (paginated)")
    public ResponseEntity<PagedResponse<SupplierReturnResponse>> getSupplierReturns(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                supplierReturnService.getByDateRange(fromDate, toDate, PageRequest.of(page, size))));
    }

}
