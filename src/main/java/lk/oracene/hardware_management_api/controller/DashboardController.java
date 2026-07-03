package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.oracene.hardware_management_api.dto.response.RecentSaleResponse;
import lk.oracene.hardware_management_api.dto.response.RevenueTrendResponse;
import lk.oracene.hardware_management_api.dto.response.SalesByCategoryResponse;
import lk.oracene.hardware_management_api.dto.response.TodaySummaryResponse;
import lk.oracene.hardware_management_api.dto.response.TopSellingProductsResponse;

import java.util.List;
import lk.oracene.hardware_management_api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard analytics APIs")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/revenue-trend")
    @Operation(summary = "Get revenue trend for the last 30 days")
    public ResponseEntity<RevenueTrendResponse> getRevenueTrend() {
        return ResponseEntity.ok(dashboardService.getRevenueTrend());
    }

    @GetMapping("/today-summary")
    @Operation(summary = "Get today's sales summary — total orders, revenue, money collected, and profit")
    public ResponseEntity<TodaySummaryResponse> getTodaySummary() {
        return ResponseEntity.ok(dashboardService.getTodaySummary());
    }

    @GetMapping("/sales-by-category")
    @Operation(summary = "Get all-time sales breakdown by category for pie chart")
    public ResponseEntity<SalesByCategoryResponse> getSalesByCategory() {
        return ResponseEntity.ok(dashboardService.getSalesByCategory());
    }

    @GetMapping("/top-selling-products")
    @Operation(summary = "Get top selling products in the last 30 days for bar chart")
    public ResponseEntity<TopSellingProductsResponse> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getTopSellingProducts(limit));
    }

    @GetMapping("/recent-sales")
    @Operation(summary = "Get the last 10 sales transactions, newest first")
    public ResponseEntity<List<RecentSaleResponse>> getRecentSales() {
        return ResponseEntity.ok(dashboardService.getRecentSales());
    }
}
