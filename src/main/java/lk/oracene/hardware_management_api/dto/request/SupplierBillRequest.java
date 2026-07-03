package lk.oracene.hardware_management_api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SupplierBillRequest {

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotBlank(message = "Bill number is required")
    @Size(max = 50, message = "Bill number must not exceed 50 characters")
    private String billNumber;

    @NotNull(message = "Bill date is required")
    private LocalDate billDate;

    private LocalDate dueDate;

    @Size(max = 500)
    private String notes;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<SupplierBillItemRequest> items;
}
