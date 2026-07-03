package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_payments")
@Data
@EqualsAndHashCode(callSuper = false)
public class SupplierPayment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long supplierPaymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_bill_id")
    private SupplierBill supplierBill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cheque_id")
    private Cheque cheque;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private SupplierPaymentMethod method;

    @Enumerated(EnumType.STRING)
    private SupplierPaymentStatus status;

    private String referenceNo;
    private LocalDateTime paidAt;
}
