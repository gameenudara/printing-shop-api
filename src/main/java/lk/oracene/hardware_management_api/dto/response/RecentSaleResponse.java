package lk.oracene.hardware_management_api.dto.response;

import lk.oracene.hardware_management_api.model.PaymentMethod;
import lk.oracene.hardware_management_api.model.SalesStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RecentSaleResponse {

    private String invoiceNumber;
    private String customerName;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private SalesStatus status;
    private LocalDateTime saleDate;
}
