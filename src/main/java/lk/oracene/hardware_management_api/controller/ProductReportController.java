package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.oracene.hardware_management_api.dto.response.MostSellingProductResponse;
import lk.oracene.hardware_management_api.dto.response.OutOfStockReportResponse.OutOfStockItem;
import lk.oracene.hardware_management_api.dto.response.PagedResponse;
import lk.oracene.hardware_management_api.dto.response.ProductStockReportResponse;
import lk.oracene.hardware_management_api.service.ProductReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/reports/product")
@RequiredArgsConstructor
@Tag(name = "Report", description = "Report APIs")
public class ProductReportController {

    private final ProductReportService productReportService;

    @GetMapping("/stock")
    @Operation(summary = "Get product stock report: total stock, selling price, purchasing price, and average margin")
    public ResponseEntity<ProductStockReportResponse> getProductStockReport() {
        return ResponseEntity.ok(productReportService.getStockReport());
    }

    @GetMapping("/out-of-stock")
    @Operation(summary = "Get all active products that are currently out of stock (paginated)")
    public ResponseEntity<PagedResponse<OutOfStockItem>> getOutOfStockReport(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                productReportService.getOutOfStockReport(PageRequest.of(page, size))));
    }

    @GetMapping("/most-selling")
    @Operation(summary = "Get most selling products in a specific time period (paginated)")
    public ResponseEntity<PagedResponse<MostSellingProductResponse>> getMostSellingProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                productReportService.getMostSellingProducts(fromDate, toDate, PageRequest.of(page, size))));
    }
}
