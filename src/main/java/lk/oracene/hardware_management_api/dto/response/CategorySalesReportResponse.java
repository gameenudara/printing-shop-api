package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CategorySalesReportResponse {

    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private List<CategorySalesRow> categories;

    @Data
    @Builder
    public static class CategorySalesRow {
        private Long categoryId;
        private String categoryName;
        private BigDecimal quantitySold;
        private BigDecimal revenue;
    }
}
