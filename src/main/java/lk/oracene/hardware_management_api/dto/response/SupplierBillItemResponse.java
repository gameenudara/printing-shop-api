package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SupplierBillItemResponse {

    private Long supplierBillItemId;
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private BigDecimal unitCostPrice;
    private BigDecimal lineTotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
