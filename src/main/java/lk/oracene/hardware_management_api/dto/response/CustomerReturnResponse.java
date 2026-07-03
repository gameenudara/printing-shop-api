package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CustomerReturnResponse {

    private Long returnId;
    private Long saleId;
    private String invoiceNumber;
    private LocalDateTime returnDate;
    private String note;
    private BigDecimal totalRefundAmount;
    private List<CustomerReturnItemResponse> items;
    private LocalDateTime createdAt;
    private String createdBy;
}
