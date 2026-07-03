package lk.oracene.hardware_management_api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lk.oracene.hardware_management_api.model.SalesStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalesResponse {

    private Long salesId;
    private String invoiceNumber;
    private String barcode;
    private Long customerId;
    private String customerName;
    private LocalDateTime saleDate;
    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal receivedAmount;
    private BigDecimal changeAmount;
    private BigDecimal remainingAmount;
    private SalesStatus status;
    private String cashier;
    private List<SalesItemResponse> items;
    private List<PaymentResponse> payments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
