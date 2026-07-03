package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cheques", indexes = {
        @Index(name = "idx_cheque_cheque_number", columnList = "cheque_number"),
        @Index(name = "idx_cheque_cheque_status", columnList = "cheque_status"),
        @Index(name = "idx_cheque_status_due_date", columnList = "cheque_status, due_date")
})
@Data
@EqualsAndHashCode(callSuper = false)
public class Cheque extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chequePaymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    private ChequeType chequeType;

    @Enumerated(EnumType.STRING)
    private ChequeStatus chequeStatus;

    private String chequeNumber;

    @Enumerated(EnumType.STRING)
    private BankName bankName;

    private String branchName;
    private BigDecimal amount;
    private LocalDate issueDate;
    private LocalDate dueDate;
}
