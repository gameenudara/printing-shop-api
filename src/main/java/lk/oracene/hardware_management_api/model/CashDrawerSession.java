package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "cash_drawer_sessions", indexes = {
        @Index(name = "idx_cash_drawer_session_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = false)
public class CashDrawerSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal openingBalance;

    @Column(precision = 12, scale = 2)
    private BigDecimal closingBalanceActual;

    @Column(precision = 12, scale = 2)
    private BigDecimal expectedBalance;

    @Column(precision = 12, scale = 2)
    private BigDecimal variance;

    @Enumerated(EnumType.STRING)
    private CashDrawerStatus status;

    private String notes;
}
