package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.response.MostSellingProductResponse;
import lk.oracene.hardware_management_api.dto.response.OutOfStockReportResponse.OutOfStockItem;
import lk.oracene.hardware_management_api.dto.response.ProductStockReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ProductReportService {

    ProductStockReportResponse getStockReport();

    Page<OutOfStockItem> getOutOfStockReport(Pageable pageable);

    Page<MostSellingProductResponse> getMostSellingProducts(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
}
