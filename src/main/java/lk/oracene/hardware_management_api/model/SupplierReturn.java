package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_returns", indexes = {
        @Index(name = "idx_supplier_return_return_date", columnList = "return_date")
})
@Data
@EqualsAndHashCode(callSuper = false)
public class SupplierReturn extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long supplierReturnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_bill_id")
    private SupplierBill supplierBill;

    private LocalDateTime returnDate;

    private BigDecimal totalReturnAmount;

    private String note;
}
