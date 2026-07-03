package lk.oracene.hardware_management_api.dto.response;

import lk.oracene.hardware_management_api.model.BankName;
import lk.oracene.hardware_management_api.model.ChequeStatus;
import lk.oracene.hardware_management_api.model.ChequeType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ChequeResponse {

    private Long chequePaymentId;
    private ChequeType chequeType;
    private ChequeStatus chequeStatus;
    private String chequeNumber;
    private BankName bankName;
    private String branchName;
    private BigDecimal amount;
    private LocalDate issueDate;
    private LocalDate dueDate;

    private Long paymentId;
    private Long customerId;
    private String customerName;
    private Long supplierId;
    private String supplierName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
