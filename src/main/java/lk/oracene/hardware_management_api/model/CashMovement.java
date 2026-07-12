package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "cash_movements", indexes = {
        @Index(name = "idx_cash_movement_session", columnList = "session_id"),
        @Index(name = "idx_cash_movement_type", columnList = "type")
})
@Data
@EqualsAndHashCode(callSuper = false)
public class CashMovement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long movementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private CashDrawerSession session;

    @Enumerated(EnumType.STRING)
    private CashMovementType type;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    private String reason;

    private Long referenceId;
}
