package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.response.CategorySalesReportResponse;
import lk.oracene.hardware_management_api.dto.response.FinancialSummaryResponse;
import lk.oracene.hardware_management_api.dto.response.ProfitLossResponse;
import lk.oracene.hardware_management_api.dto.response.SalesReportResponse;

import java.time.LocalDateTime;

public interface SalesReportService {

    SalesReportResponse getSalesReport(LocalDateTime fromDate, LocalDateTime toDate);

    FinancialSummaryResponse getTodayFinancialSummary();

    CategorySalesReportResponse getCategorySalesReport(LocalDateTime fromDate, LocalDateTime toDate);

    ProfitLossResponse getProfitLoss(LocalDateTime fromDate, LocalDateTime toDate);
}
