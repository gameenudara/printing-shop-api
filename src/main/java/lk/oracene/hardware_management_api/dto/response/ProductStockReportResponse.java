package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductStockReportResponse {

    private long totalProducts;
    private BigDecimal totalStockQuantity;
    private BigDecimal totalSellingPrice;
    private BigDecimal totalPurchasingPrice;
    private BigDecimal averageMargin;
}
