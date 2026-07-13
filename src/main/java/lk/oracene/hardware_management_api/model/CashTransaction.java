package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "cash_transactions", indexes = {
        @Index(name = "idx_cash_transaction_session", columnList = "session_id"),
        @Index(name = "idx_cash_transaction_type", columnList = "type")
})
@Data
@EqualsAndHashCode(callSuper = false)
public class CashTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private CashDrawerSession session;

    @Enumerated(EnumType.STRING)
    private CashTransactionType type;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    private String reason;

    private Long referenceId;
}
