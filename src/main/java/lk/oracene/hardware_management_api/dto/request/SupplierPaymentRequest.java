package lk.oracene.hardware_management_api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.oracene.hardware_management_api.model.BankName;
import lk.oracene.hardware_management_api.model.SupplierPaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SupplierPaymentRequest {

    @NotNull(message = "Supplier bill ID is required")
    private Long supplierBillId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private SupplierPaymentMethod method;

    @Size(max = 100)
    private String referenceNo;

    // Required only when method = CHEQUE
    private String chequeNumber;
    private BankName bankName;
    private String branchName;
    private LocalDate chequeIssueDate;
    private LocalDate chequeDueDate;
}
