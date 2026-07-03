package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.response.RecentSaleResponse;
import lk.oracene.hardware_management_api.dto.response.RevenueTrendResponse;
import lk.oracene.hardware_management_api.dto.response.SalesByCategoryResponse;
import lk.oracene.hardware_management_api.dto.response.TodaySummaryResponse;
import lk.oracene.hardware_management_api.dto.response.TopSellingProductsResponse;

import java.util.List;

public interface DashboardService {

    RevenueTrendResponse getRevenueTrend();

    TodaySummaryResponse getTodaySummary();

    SalesByCategoryResponse getSalesByCategory();

    TopSellingProductsResponse getTopSellingProducts(int limit);

    List<RecentSaleResponse> getRecentSales();
}
