package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "supplier_bills", indexes = {
        @Index(name = "idx_supplier_bill_bill_number", columnList = "bill_number"),
        @Index(name = "idx_supplier_bill_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = false)
public class SupplierBill extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long supplierBillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    private String billNumber;
    private LocalDate billDate;
    private LocalDate dueDate;

    private BigDecimal totalAmount;
    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    private SupplierBillStatus status;

    private String notes;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalReturnAmount = BigDecimal.ZERO;
}
