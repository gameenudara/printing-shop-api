package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OutOfStockReportResponse {

    private long totalOutOfStockCount;
    private List<OutOfStockItem> products;

    @Data
    @Builder
    public static class OutOfStockItem {
        private Long productId;
        private String name;
        private String sku;
        private String brand;
        private String colour;
        private String size;
        private String categoryName;
        private BigDecimal unitPrice;
    }
}
